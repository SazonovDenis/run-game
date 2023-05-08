package run.game.dao.backstage

import jandcode.commons.*
import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.dbm.mdb.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*

public class TaskCreatorImpl extends BaseMdbUtils implements TaskCreator {

    StoreIndex idxDataType
    Store st_wordRus
    Rnd rnd

    void setMdb(Mdb mdb) {
        super.setMdb(mdb)

        //
        Store stDataType = mdb.loadQuery("select * from DataType")
        idxDataType = stDataType.getIndex("code")

        // Источники неправильных ответов
        st_wordRus = mdb.loadQuery("select * from Fact where dataType = " + idxDataType.get("word-translate").getLong("id"))

        //
        rnd = new RndImpl(0)
    }

    public DataBox createTask(long idItem, String dataTypeQuestion, String dataTypeAnswer) {
        DataBox res = new DataBox()

        // Загружаем факты "вопрос" и "ответ"
        Store stQuestion = mdb.loadQuery(sqlFactValue(), [id: idItem, dataType: dataTypeQuestion])
        Store stAnswer = mdb.loadQuery(sqlFactValue(), [id: idItem, dataType: dataTypeAnswer])

        //
        if (stQuestion.size() == 0) {
            throw new Exception("Не найден dataTypeQuestion: " + dataTypeQuestion)
        }
        if (stAnswer.size() == 0) {
            throw new Exception("Не найден dataTypeAnswer: " + dataTypeAnswer)
        }

        // ---
        // Типа запросили пару "word-translate -> word-spelling"
        // ---

        // Выбираем "факт вопрос" и "факт ответ"
        StoreRecord recQuestion = stQuestion.get(rnd.num(0, stQuestion.size() - 1))
        StoreRecord recAnswer = stAnswer.get(rnd.num(0, stAnswer.size() - 1))
        StoreIndex idxAnswer = stAnswer.getIndex("factValue")
        //mdb.outTable(recQuestion)
        //mdb.outTable(recAnswer)

        //
        String valueTrue = recAnswer.getValue("factValue")


        // ---
        // Генерим варианты ответов
        int optionsCount = 6
        int valuesFalseChoiceCount = 20

        // Подберем неправильные варианты
        Map valuesFalseMap = UtWordDistance.getJaroWinklerMatch(valueTrue, st_wordRus, valuesFalseChoiceCount)
        Set valuesFalseSet = valuesFalseMap.keySet()


        // ---
        // Фильтруем неправильные варианты
        String[] valueTrueWords = valueTrue.split("[ ,;)(]")

        // Формируем синонимы правильного ответа,
        // чтобы исключить их из "ложных" вариантов (иначе пользователь запутается)
        Set set_synonyms = new HashSet()
        for (String valueTrueWord : valueTrueWords) {
            if (valueTrueWord.length() == 0) {
                continue
            }
            StoreRecord rec_synonyms = mdb.loadQueryRecord("select * from WordSynonym where word = :word and lang = :lang", [word: valueTrueWord, lang: "rus"], false)
            if (rec_synonyms != null) {
                String strSynonymsJson = new String(rec_synonyms.getValue("synonyms"), "utf-8")
                Collection collSynonymsJson = UtJson.fromJson(strSynonymsJson)
                set_synonyms.addAll(collSynonymsJson)
            }
        }

        // Фильтруем по совпадению хотя-бы одного слова из правильного ответа
        // с хотя бы одним словом из неправильного варианта
        Set set_maches = new HashSet()
        for (String valueTrueWord : valueTrueWords) {
            if (valueTrueWord.length() == 0) {
                continue
            }

            for (String valueFalse : valuesFalseSet) {
                String[] valueFalseWords = valueFalse.split("[ ,;)(]")
                for (String valueFalseWord : valueFalseWords) {
                    if (valueFalseWord.length() == 0) {
                        continue
                    }

                    if (valueFalseWord.equalsIgnoreCase(valueTrueWord)) {
                        set_maches.add(valueFalse)
                        break
                    }
                }
            }
        }

        // Фильтруем синонимы и совпадения
        List<String> valuesFalseArr = new ArrayList<>()
        for (String falseValue : valuesFalseSet) {
            if (!set_synonyms.contains(falseValue) && !set_maches.contains(falseValue)) {
                valuesFalseArr.add(falseValue)
            }
        }


        // ---
        // Всё нормально?
        int falseOptionsTotalCount = valuesFalseArr.size()
        if (falseOptionsTotalCount <= optionsCount) {
            throw new Exception("Не нашлось достаточного количества неправильных ответов, question: '" + recQuestion.getValue("factValue") + "', answer: '" + valueTrue + "'")
        }


        // ---
        // Формируем recTask
        StoreRecord recTask = mdb.createStoreRecord("Task.question")
        recTask.setValue("item", idItem)
        recTask.setValue("factQuestion", recQuestion.getValue("fact"))
        recTask.setValue("factAnswer", recAnswer.getValue("fact"))
        //
        recTask.setValue("dataType", recQuestion.getValue("factDataType"))
        recTask.setValue("value", recQuestion.getValue("factValue"))


        // ---
        // Формируем stTaskOption: готовим правильный и неправильные ответы

        // Выбираем неправильные ответы (и их порядок)
        int n = 0
        int[] falseOntionsIndexes = new int[optionsCount]
        Set<Integer> falseOptionsIndexesUsed = new HashSet<>()
        while (falseOptionsIndexesUsed.size() < optionsCount) {
            int falseOptionIndex = rnd.num(0, falseOptionsTotalCount - 1)
            // Обеспечим, что среди неправильных вариантов не окажется совпадающего с
            // одним из правильных (он может оказаться отобраным
            // функцией UtWordDistance.getJaroWinklerMatch по похожести)
            String valueFalse = valuesFalseArr.get(falseOptionIndex)
            boolean valueFalseIsCoincideTrue = idxAnswer.get(valueFalse) != null
            //
            if (!falseOptionsIndexesUsed.contains(falseOptionIndex) && !valueFalseIsCoincideTrue) {
                falseOptionsIndexesUsed.add(falseOptionIndex)
                falseOntionsIndexes[n] = falseOptionIndex
                n = n + 1
            }
        }

        // Выбираем позицию правильного варианта в задании
        int trueValuePos = rnd.num(0, optionsCount - 1)

        // Заполняем stTaskOption вариантами, среди которых один правильный
        Store stTaskOption = mdb.createStore("TaskOption")
        for (int i = 0; i < optionsCount; i++) {
            StoreRecord recOption = stTaskOption.add()
            if (i == trueValuePos) {
                // Правильный ответ
                recOption.setValue("trueFact", recAnswer.getValue("fact"))
                recOption.setValue("dataType", recAnswer.getValue("factDataType"))
                recOption.setValue("value", valueTrue)
            } else {
                // Неправильные варианты
                int falseOptionIndex = falseOntionsIndexes[i]
                String valueFalse = valuesFalseArr[falseOptionIndex]
                //
                recOption.setValue("dataType", recAnswer.getValue("factDataType"))
                recOption.setValue("value", valueFalse)
            }
        }


        // ---
        // Формируем stTaskValue: звуковое сопровождение
        Store stTaskValue = mdb.createStore("TaskValue")
        if (dataTypeQuestion.equals(DbConst.DataType_CODE_word_spelling)) {
            // Загружаем факты "звук"
            Store stQuestionTaskValue = mdb.loadQuery(sqlFactValue(), [id: idItem, dataType: DbConst.DataType_CODE_word_sound])
            // Выбираем факт "звук"
            if (stQuestionTaskValue.size() != 0) {
                StoreRecord recQuestionTaskValue = stQuestionTaskValue.get(rnd.num(0, stQuestionTaskValue.size() - 1))
                StoreRecord recTaskValue = stTaskValue.add()
                recTaskValue.setValue("dataType", recQuestionTaskValue.getValue("factDataType"))
                recTaskValue.setValue("value", recQuestionTaskValue.getValue("factValue"))

            }
        }


        // ---
        res.put("task", recTask)
        res.put("taskOption", stTaskOption)
        res.put("taskValue", stTaskValue)

        //
        return res
    }


    public DataBox loadTask(long idTask) {
        DataBox res = new DataBox()

        //
        StoreRecord recTask = mdb.createStoreRecord("Task.question")
        Store stTaskOption = mdb.createStore("TaskOption")
        Store stTaskValue = mdb.createStore("TaskValue")
        mdb.loadQueryRecord(recTask, sqlFactForTask(), [id: idTask])
        mdb.loadQuery(stTaskOption, sqlTaskOption(), [id: idTask])
        mdb.loadQuery(stTaskValue, sqlTaskValue(), [id: idTask])

        //
        res.put("task", recTask)
        res.put("taskOption", stTaskOption)
        res.put("taskValue", stTaskValue)

        //
        return res
    }

    public long saveTask(DataBox task) {
        StoreRecord recTask = mdb.createStoreRecord("Task", task.get("task"))
        Store stTaskOption = task.get("taskOption")
        Store stTaskValue = task.get("taskValue")

        //
        long idTask = mdb.insertRec("Task", recTask)

        //
        for (StoreRecord rec : stTaskOption) {
            StoreRecord recTaskOption = mdb.createStoreRecord("TaskOption")
            recTaskOption.setValues(rec.getValues())
            recTaskOption.setValue("task", idTask)
            mdb.insertRec("TaskOption", recTaskOption)
        }

        //
        for (StoreRecord rec : stTaskValue) {
            StoreRecord recTaskValue = mdb.createStoreRecord("TaskValue")
            recTaskValue.setValues(rec.getValues())
            recTaskValue.setValue("task", idTask)
            mdb.insertRec("TaskValue", recTaskValue)
        }

        return idTask
    }

    String sqlFactForTask() {
        return """
select 
    Task.*,
    Fact.dataType, 
    Fact.value 
from 
    Task
    join Fact on (Task.factQuestion = Fact.id)
where 
    Task.id = :id 
"""
    }

    String sqlTaskOption() {
        return """
select 
    *
from 
    TaskOption
where
    TaskOption.task = :id 
"""
    }

    String sqlTaskValue() {
        return """
select 
    *
from 
    TaskValue
where
    TaskValue.task = :id 
"""
    }

    String sqlFactValue() {
        return """
select
    Item.id item,
    Item.value itemValue,
    Fact.id fact,

    Fact.dataType factDataType,
    Fact.value factValue,

    FactTag.id factTagId,
    FactTag_TagType.code factTag_code,
    FactTag_Tag.value factTag,

    FactTagValue.id factTagValueId,
    FactTagValue_TagType.code factTagValue_code,
    FactTagValue.value factTagValue_Value
from
    Item

    left join Fact on (Fact.item = Item.id)
    left join DataType Fact_DataType on (Fact.dataType = Fact_DataType.id)

    left join FactTag on (FactTag.fact = Fact.id)
    left join Tag FactTag_Tag on (FactTag.tag = FactTag_Tag.id)
    left join TagType FactTag_TagType on (FactTag_Tag.tagType = FactTag_TagType.id)

    left join FactTagValue on (FactTagValue.fact = Fact.id)
    left join TagType FactTagValue_TagType on (FactTagValue.tagType = FactTagValue_TagType.id)
where
    Item.id = :id and
    Fact_DataType.code = :dataType

order by
    Item.id,
    Fact.dataType,
    Fact.id,
    FactTag.tag
"""

    }

}
