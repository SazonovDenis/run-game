package run.game.dao.backstage

import groovy.transform.*
import jandcode.commons.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.util.*

@TypeChecked
class FactDataLoader extends RgmMdbUtils {


    static Map<Long, String> factTypeFieldNames = [
            (RgmDbConst.FactType_word_spelling) : "valueSpelling",
            (RgmDbConst.FactType_word_translate): "valueTranslate",
            (RgmDbConst.FactType_word_sound)    : "valueSound",
            (RgmDbConst.FactType_word_picture)  : "valuePicture",
    ]


    // region fillFactBody Дополняет факты "богатыми" данными вопроса и ответа (звук, перевод и т.п.)

    /**
     Семейство методов fillFactBody***
     Дополняет факты "богатыми" данными вопроса и ответа (звук, перевод и т.п.),
     т.е. заполним поля question и answer.
     Расчитываем, что в stTasks есть поля factQuestion и factAnswer.
     */

    /**
     */
    void fillFactBody(Store stTasks) {
        // Дополним факты в плане "богатыми" данными для вопроса и ответа
        Set itemIds = stTasks.getUniqueValues("item")
        Set factQuestionIds = stTasks.getUniqueValues("factQuestion")
        Set factAnswerIds = stTasks.getUniqueValues("factAnswer")

        // Факты каждого типа для списка itemIds
        Store stFactData_question = mdb.loadQuery(sqlFactData_Facts(factQuestionIds, "factQuestion"))
        Store stFactData_answer = mdb.loadQuery(sqlFactData_Facts(factAnswerIds, "factAnswer"))
        Store stFactData_sound = mdb.loadQuery(sqlFactData_Items(itemIds, RgmDbConst.FactType_word_sound))

        // Размажем
        convertFactsToFlatRecord(stFactData_question, ["factQuestion"], stTasks, "question")
        convertFactsToFlatRecord(stFactData_answer, ["factAnswer"], stTasks, "answer")
        convertFactsToFlatRecord(stFactData_sound, ["item"], stTasks, "question")
    }

    /**
     */
    void fillFactBodyPlan(Store stTasks, long idPlan) {
        // Факты вопроса и ответа
        Store stFactData_Question = mdb.loadQuery(sqlFactData_PlanQuestion(), [plan: idPlan])
        Store stFactData_Answer = mdb.loadQuery(sqlFactData_PlanAnswer(), [plan: idPlan])

        // Размажем факты вопроса и ответа
        convertFactsToFlatRecord(stFactData_Question, ["factQuestion"], stTasks, "question")
        convertFactsToFlatRecord(stFactData_Answer, ["factAnswer"], stTasks, "answer")

        // Дополнительные факты Item
        Store stFactData_Item = mdb.loadQuery(sqlFactData_PlanItem(), [plan: idPlan])
        convertFactsToFlatRecord(stFactData_Item, ["factQuestion"], stTasks, "question")
    }

    /**
     */
    void fillFactBodyGame(Store stTasks, long idGame, long idUsr) {
        // Данные вопроса и ответа
        Store sqlFactData_Question = mdb.loadQuery(sqlFactData_GameTaskQuestion(), [game: idGame, usr: idUsr])
        Store sqlFactData_Answer = mdb.loadQuery(sqlFactData_GameTaskAnswer(), [game: idGame, usr: idUsr])

        // Размажем
        convertFactsToFlatRecord(sqlFactData_Question, ["task"], stTasks, "taskQuestion")
        convertFactsToFlatRecord(sqlFactData_Answer, ["task"], stTasks, "taskAnswer")
    }

    // endregion


    // region convertToFlatRecord Запись типа Fact раскладываем в "плоскую" запись

    /**
     Семейство методов convertFactsToFlatRecord
     Запись типа Fact раскладываем в "плоскую" запись.
     Берем пару полей recFact.factType+recFact.value и заполняем
     соответствующее поле (valueSound, valueTranslate, valueSpelling или valuePicture),
     в зависимости от recFact.factType.
     */

    void convertFactToFlatRecord(StoreRecord recFact, StoreRecord recTask) {
        long sourceDatatype = recFact.getLong("factType")
        String destFieldName = factTypeFieldNames.get(sourceDatatype)
        if (UtCnv.isEmpty(destFieldName)) {
            return
        }

        // Если поле уже заполнено - не заполняем
        if (!recTask.isValueNull(destFieldName)) {
            return
        }

        // Если поле "valueSpelling" уже заполнено - не заполняем "valueTranslate"
        if (sourceDatatype == RgmDbConst.FactType_word_spelling && !recTask.isValueNull("valueTranslate")) {
            return
        }

        // Если поле "valueSpelling" уже заполнено - не заполняем "valueTranslate"
        if (sourceDatatype == RgmDbConst.FactType_word_translate && !recTask.isValueNull("valueSpelling")) {
            return
        }

        //
        recTask.setValue(destFieldName, recFact.getValue("value"))
    }

    void convertFactsToFlatRecord(Store stFactData, Collection<String> keyFields, Store stDest, String fieldDest) {
        Map<Object, List<StoreRecord>> mapFacts = StoreUtils.collectGroupBy_records(stFactData, keyFields)

        //
        for (StoreRecord recDest : stDest) {
            String key = StoreUtils.concatenateFields(recDest, keyFields)

            //
            StoreRecord recTaskQuestion = mdb.createStoreRecord("Task.fields")
            recTaskQuestion.setValues(recDest.getValue(fieldDest))

            //
            List<StoreRecord> lstFact = mapFacts.get(key)
            for (StoreRecord recFact : lstFact) {
                convertFactToFlatRecord(recFact, recTaskQuestion)
            }

            //
            recDest.setValue(fieldDest, recTaskQuestion.getValues())
        }

    }
    // endregion


    // region sql

    String sqlFactData_PlanItem() {
        return """
select 
    PlanFact.factQuestion, 
    PlanFact.factAnswer, 
    
    Fact.item,
    Fact.factType,
    Fact.value

from 
    PlanFact
    -- Факт, который привязан к PlanFact.factQuestion
    join Fact PlanFact_Fact on (
        PlanFact.factQuestion = PlanFact_Fact.id
    )
    -- Факты нужных типов для Fact.item
    join Fact on (
        PlanFact_Fact.item = Fact.item and
        Fact.factType in (${RgmDbConst.FactType_word_spelling + "," + RgmDbConst.FactType_word_sound})
    )

where
    PlanFact.plan = :plan 
"""
    }

    String sqlFactData_Items(Set items, long factType) {
        String itemsStr = "0"
        if (items.size() > 0) {
            itemsStr = items.join(",")
        }

        return """ 
-- Значения фактов типа factType, по списку сущностей
select 
    Item.id item,
    Fact.id fact,
    Fact.factType factType,
    Fact.value value

from    
    Item
    -- Факт типа factType
    join Fact on (
        Item.id = Fact.item and
        Fact.factType = ${factType}
    )
    
where
    Item.id in (${itemsStr})    
"""
    }

    String sqlFactData_Facts(Set factIds, String factFieldKeyName) {
        String factsStr = "0"
        if (factIds.size() > 0) {
            factsStr = factIds.join(",")
        }
        if (factFieldKeyName == null) {
            factFieldKeyName = "fact"
        }

        return """ 
-- Значения фактов, по списку фактов
select 
    Fact.item item,
    Fact.id as ${factFieldKeyName},
    Fact.factType factType,
    Fact.value value

from    
    Fact
    
where
    Fact.id in (${factsStr})    
"""
    }

    String sqlFactData_GameTaskQuestion() {
        return """
select 
    GameTask.*,
    TaskQuestion.factType,
    TaskQuestion.value

from 
    GameTask
    join TaskQuestion on (
        TaskQuestion.task = GameTask.task
    )

where
    GameTask.usr = :usr and 
    GameTask.game = :game 
"""
    }

    String sqlFactData_GameTaskAnswer() {
        return """
select 
    GameTask.*,
    TaskOption.factType,
    TaskOption.value

from 
    GameTask
    join TaskOption on (
        TaskOption.task = GameTask.task and 
        TaskOption.isTrue = 1 
    )

where
    GameTask.usr = :usr and 
    GameTask.game = :game 
"""
    }

    String sqlFactData_PlanQuestion() {
        return """
select 
    PlanFact.factQuestion, 
    PlanFact.factAnswer, 
    
    Fact.item,
    Fact.factType,
    Fact.value

from 
    PlanFact
    -- Факт, который привязан к PlanFact.factQuestion
    join Fact on (
        PlanFact.factQuestion = Fact.id
    )

where
    PlanFact.plan = :plan 
"""
    }

    String sqlFactData_PlanAnswer() {
        return """
select 
    PlanFact.factQuestion, 
    PlanFact.factAnswer, 
    
    Fact.factType,
    Fact.value

from 
    PlanFact
    -- Факт, который привязан к PlanFact.factAnswer
    join Fact on (
        PlanFact.factAnswer = Fact.id
    )

where
    PlanFact.plan = :plan 
"""
    }

    // endregion

}
