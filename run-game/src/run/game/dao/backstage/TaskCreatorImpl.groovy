package run.game.dao.backstage


import jandcode.commons.*
import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.dbm.mdb.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*


public class TaskCreatorImpl extends RgMdbUtils implements TaskCreator {

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
        // ---
        // Типа запросили пару "word-translate -> word-spelling"
        // ---

        // Загружаем список фактов для "вопроса" и "ответа"
        Fact_list list = mdb.create(Fact_list)
        Store stQuestion = list.loadFactsByDataType(idItem, dataTypeQuestion)
        Store stAnswer = list.loadFactsByDataType(idItem, dataTypeAnswer)

        //
        if (stQuestion.size() == 0) {
            throw new Exception("Не найден dataTypeQuestion: " + dataTypeQuestion)
        }
        if (stAnswer.size() == 0) {
            throw new Exception("Не найден dataTypeAnswer: " + dataTypeAnswer)
        }

        // Выбираем "факт вопрос" и "факт ответ"
        int idxFactQuestion = rnd.num(0, stQuestion.size() - 1)
        int idxFactAnswer = rnd.num(0, stAnswer.size() - 1)
        long idFactQuestion = stQuestion.get(idxFactQuestion).getLong("id")
        long idFactAnswer = stAnswer.get(idxFactAnswer).getLong("id")

        // Формируем задание
        return createTask(idFactQuestion, idFactAnswer)
    }

    public DataBox createTask(long idFactQuestion, long idFactAnswer) {
        DataBox res = new DataBox()

        //
        Fact_list list = mdb.create(Fact_list)
        StoreRecord recFactQuestion = list.loadFact(idFactQuestion)
        StoreRecord recFactAnswer = list.loadFact(idFactAnswer)
        //
        long idItem = recFactQuestion.getLong("item")
        long dataTypeQuestion = recFactQuestion.getLong("factDataType")
        long dataTypeAnswer = recFactAnswer.getLong("factDataType")
        String valueTrue = recFactAnswer.getValue("factValue")


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
            throw new Exception("Не нашлось достаточного количества неправильных ответов, question: '" + recFactQuestion.getValue("factValue") + "', answer: '" + valueTrue + "'")
        }


        // ---
        // Формируем recTask
        StoreRecord recTask = mdb.createStoreRecord("Task")
        recTask.setValue("item", idItem)
        recTask.setValue("factQuestion", recFactQuestion.getValue("id"))
        recTask.setValue("factAnswer", recFactAnswer.getValue("id"))


        // ---
        // Формируем stTaskOption: готовим правильный и неправильные ответы

        // Выбираем неправильные ответы (и их порядок)
        Store stAnswer = list.loadFactsByDataType(idItem, dataTypeAnswer)
        StoreIndex idxAnswer = stAnswer.getIndex("factValue")
        //
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
                recOption.setValue("isTrue", true)
                recOption.setValue("dataType", recFactAnswer.getValue("factDataType"))
                recOption.setValue("value", valueTrue)
            } else {
                // Неправильные варианты
                int falseOptionIndex = falseOntionsIndexes[i]
                String valueFalse = valuesFalseArr[falseOptionIndex]
                //
                recOption.setValue("dataType", recFactAnswer.getValue("factDataType"))
                recOption.setValue("value", valueFalse)
            }
        }


        // ---
        // Формируем stTaskQuestion: основной вопрос
        Store stTaskQuestion = mdb.createStore("TaskQuestion")


        // Формируем основной вопрос
        StoreRecord recTaskQuestion = stTaskQuestion.add()
        recTaskQuestion.setValue("dataType", recFactQuestion.getValue("factDataType"))
        recTaskQuestion.setValue("value", recFactQuestion.getValue("factValue"))


        // Формируем дополнительную информацию
        if (dataTypeQuestion == DbConst.DataType_word_spelling) {
            // Загружаем факты "звук"
            Store stFact = list.loadFactsByDataType(idItem, DbConst.DataType_word_sound)
            // Выбираем факт "звук"
            if (stFact.size() != 0) {
                int idx = rnd.num(0, stFact.size() - 1)
                StoreRecord recFact = stFact.get(idx)
                recTaskQuestion = stTaskQuestion.add()
                recTaskQuestion.setValue("dataType", recFact.getValue("factDataType"))
                recTaskQuestion.setValue("value", recFact.getValue("factValue"))

            }
        }
        if (dataTypeQuestion == DbConst.DataType_word_sound) {
            // Загружаем факты "написание"
            Store stFact = list.loadFactsByDataType(idItem, DbConst.DataType_word_spelling)
            // Выбираем факт "написание"
            if (stFact.size() != 0) {
                int idx = rnd.num(0, stFact.size() - 1)
                StoreRecord recFact = stFact.get(idx)
                recTaskQuestion = stTaskQuestion.add()
                recTaskQuestion.setValue("dataType", recFact.getValue("factDataType"))
                recTaskQuestion.setValue("value", recFact.getValue("factValue"))
            }
        }


        // ---
        res.put("task", recTask)
        res.put("taskOption", stTaskOption)
        res.put("taskQuestion", stTaskQuestion)

        //
        return res
    }


    public DataBox loadTask(long idTask) {
        DataBox res = new DataBox()

        //
        StoreRecord recTask = mdb.createStoreRecord("Task.TaskCreator")
        Store stTaskOption = mdb.createStore("TaskOption")
        Store stTaskQuestion = mdb.createStore("TaskQuestion")
        mdb.loadQueryRecord(recTask, sqlFactForTask(), [id: idTask])
        mdb.loadQuery(stTaskOption, sqlTaskOption(), [id: idTask])
        mdb.loadQuery(stTaskQuestion, sqlTaskQuestion(), [id: idTask])

        //
        res.put("task", recTask)
        res.put("taskOption", stTaskOption)
        res.put("taskQuestion", stTaskQuestion)

        //
        return res
    }

    public long saveTask(DataBox task) {
        StoreRecord recTask = mdb.createStoreRecord("Task", task.get("task"))
        Store stTaskOption = task.get("taskOption")
        Store stTaskQuestion = task.get("taskQuestion")

        //
        long idTask = mdb.insertRec("Task", recTask)

        //
        for (StoreRecord rec : stTaskOption) {
            StoreRecord recIns = mdb.createStoreRecord("TaskOption")
            recIns.setValues(rec.getValues())
            recIns.setValue("task", idTask)
            mdb.insertRec("TaskOption", recIns)
        }

        //
        for (StoreRecord rec : stTaskQuestion) {
            StoreRecord recIns = mdb.createStoreRecord("TaskQuestion")
            recIns.setValues(rec.getValues())
            recIns.setValue("task", idTask)
            mdb.insertRec("TaskQuestion", recIns)
        }

        //
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

    String sqlTaskQuestion() {
        return """
select 
    *
from 
    TaskQuestion
where
    TaskQuestion.task = :id 
"""
    }


}
