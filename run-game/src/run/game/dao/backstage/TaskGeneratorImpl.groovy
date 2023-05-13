package run.game.dao.backstage

import jandcode.commons.*
import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.dbm.mdb.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*

public class TaskGeneratorImpl extends RgmMdbUtils implements TaskGenerator {

    Rnd rnd

    void setMdb(Mdb mdb) {
        super.setMdb(mdb)

        //
        rnd = new RndImpl(0)
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
        //Map valuesFalseMap = UtWordDistance.getJaroWinklerMatch(valueTrue, stWordRus, valuesFalseChoiceCount)
        //Set valuesFalseSet = valuesFalseMap.keySet()
        Set valuesFalseSet = new HashSet()
        StoreRecord recWordDistance = mdb.loadQueryRecord("select * from WordDistance where word = :word", [word: valueTrue], false)
        if (recWordDistance != null) {
            String strJson = new String(recWordDistance.getValue("matches"), "utf-8")
            Map map = UtJson.fromJson(strJson)
            valuesFalseSet.addAll(map.keySet())
        }


        // ---
        // Фильтруем неправильные варианты
        String[] valueTrueWords = valueTrue.split("[ ,;)(]")

        // Формируем синонимы правильного ответа,
        // чтобы исключить их из "ложных" вариантов (иначе пользователь запутается)
        Set synonymsSet = new HashSet()
        for (String valueTrueWord : valueTrueWords) {
            if (valueTrueWord.length() == 0) {
                continue
            }
            StoreRecord recWordSynonym = mdb.loadQueryRecord("select * from WordSynonym where word = :word", [word: valueTrueWord], false)
            if (recWordSynonym != null) {
                String strJson = new String(recWordSynonym.getValue("synonyms"), "utf-8")
                Collection collection = UtJson.fromJson(strJson)
                synonymsSet.addAll(collection)
            }
        }

        // Фильтруем по совпадению хотя-бы одного слова из правильного ответа
        // с хотя бы одним словом из неправильного варианта
        Set matchesSet = new HashSet()
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
                        matchesSet.add(valueFalse)
                        break
                    }
                }
            }
        }

        // Фильтруем синонимы и совпадения
        List<String> valuesFalseArr = new ArrayList<>()
        for (String falseValue : valuesFalseSet) {
            if (!synonymsSet.contains(falseValue) && !matchesSet.contains(falseValue)) {
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
        if (dataTypeQuestion != RgmDbConst.DataType_word_sound) {
            // Загружаем факты "звук"
            Store stFact = list.loadFactsByDataType(idItem, RgmDbConst.DataType_word_sound)
            // Выбираем факт "звук"
            if (stFact.size() != 0) {
                int idx = rnd.num(0, stFact.size() - 1)
                StoreRecord recFact = stFact.get(idx)
                recTaskQuestion = stTaskQuestion.add()
                recTaskQuestion.setValue("dataType", recFact.getValue("factDataType"))
                recTaskQuestion.setValue("value", recFact.getValue("factValue"))

            }
        }
        if (dataTypeQuestion != RgmDbConst.DataType_word_spelling) {
            // Загружаем факты "написание"
            Store stFact = list.loadFactsByDataType(idItem, RgmDbConst.DataType_word_spelling)
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

    /**
     * Создает задания и неправильные варианты ответа к ним.
     *
     * @param idItem Дла какой сущности
     * @param tagQuestion Факт какого типа пойдет как вопрос
     * @param tagAnswer Факт какого типа пойдет как ответ
     * @return Список {task: rec, options: [rec]}
     */
    public Collection<DataBox> createTasks(long idItem, String dataTypeQuestion, String dataTypeAnswer) {
        Collection<DataBox> res = new ArrayList<>()

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

        // Перебираем факты: "факт вопрос" и "факт ответ", для каждой пары
        for (StoreRecord recQuestion : stQuestion) {
            long idFactQuestion = recQuestion.getLong("id")

            for (StoreRecord recAnswer : stAnswer) {
                long idFactAnswer = recAnswer.getLong("id")

                // Формируем задание
                DataBox task = createTask(idFactQuestion, idFactAnswer)
                res.add(task)
            }
        }

        //
        return res
    }


}
