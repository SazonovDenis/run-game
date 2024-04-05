package run.game.dao.backstage

import jandcode.commons.*
import jandcode.commons.error.*
import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.dbm.mdb.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.model.service.*
import run.game.testdata.fixture.*

public class TaskGeneratorImpl extends RgmMdbUtils implements TaskGenerator {

    Rnd rnd

    int OPTIONS_COUNT = 6
    int VALUES_FALSE_MAX_COUNT = 25

    Map<Long, String> DataType_CODE = [
            (RgmDbConst.DataType_word_spelling) : "word_spelling",
            (RgmDbConst.DataType_word_translate): "word_translate",
            (RgmDbConst.DataType_word_sound)    : "word_sound",
            (RgmDbConst.DataType_word_picture)  : "word_picture",
    ]


    void setMdb(Mdb mdb) {
        super.setMdb(mdb)

        //
        rnd = new RndImpl(0)
    }


    @Override
    public DataBox generateTask(long factQuestion, long factAnswer, Collection<String> answerFalseValues) {
        DataBox res = new DataBox()

        //
        Fact_list list = mdb.create(Fact_list)
        StoreRecord recFactQuestion = list.loadFact(factQuestion)
        StoreRecord recFactAnswer = list.loadFact(factAnswer)
        //
        long idItem = recFactQuestion.getLong("item")
        long dataTypeQuestion = recFactQuestion.getLong("factDataType")
        long dataTypeAnswer = recFactAnswer.getLong("factDataType")
        String valueTrue = recFactAnswer.getValue("factValue")


        // ---
        // Генерим варианты ответов


        // ---
        // Подберем неправильные варианты
        Set<String> valuesFalseSet = prepareValuesFalse(valueTrue, answerFalseValues)


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
                String synonymsJson = new String(recWordSynonym.getValue("synonyms"), "utf-8")
                Collection synonymsList = (Collection) UtJson.fromJson(synonymsJson)
                synonymsSet.addAll(synonymsList)
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
        if (falseOptionsTotalCount <= OPTIONS_COUNT) {
            throw new XError(
                    "Не удалось подобрать достаточного ({0}) количества неправильных ответов, {1} -> {2}, question: \"{3}\", answer: \"{4}\", valuesFalse: {5}",
                    OPTIONS_COUNT,
                    DataType_CODE[dataTypeQuestion],
                    DataType_CODE[dataTypeAnswer],
                    recFactQuestion.getValue("factValue"),
                    valueTrue,
                    "\"" + UtString.join(valuesFalseSet, "\", \"") + "\""
            )
        }


        // ---
        // Формируем recTask
        StoreRecord recTask = mdb.createStoreRecord("Task")
        recTask.setValue("factQuestion", recFactQuestion.getValue("id"))
        recTask.setValue("factAnswer", recFactAnswer.getValue("id"))


        // ---
        // Формируем stTaskOption: готовим правильный и неправильные ответы

        // Выбираем неправильные ответы (и их порядок)
        Store stAnswer = list.loadFactsByDataType(idItem, dataTypeAnswer)
        StoreIndex idxAnswer = stAnswer.getIndex("factValue")
        //
        int n = 0
        int[] falseOntionsIndexes = new int[OPTIONS_COUNT]
        Set<Integer> falseOptionsIndexesUsed = new HashSet<>()
        while (falseOptionsIndexesUsed.size() < OPTIONS_COUNT) {
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
        int trueValuePos = rnd.num(0, OPTIONS_COUNT - 1)

        // Заполняем stTaskOption вариантами, среди которых один правильный
        Store stTaskOption = mdb.createStore("TaskOption")
        for (int i = 0; i < OPTIONS_COUNT; i++) {
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
        res.put("taskQuestion", stTaskQuestion)
        res.put("taskOption", stTaskOption)


        //
        return res
    }


    public DataBox createTask___(long idFactQuestion, long idFactAnswer) {
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
        Set<String> valuesFalseSet = prepareValuesFalse(valueTrue)


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
                String synonymsJson = new String(recWordSynonym.getValue("synonyms"), "utf-8")
                Collection synonymsList = (Collection) UtJson.fromJson(synonymsJson)
                synonymsSet.addAll(synonymsList)
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
            throw new XError("Не нашлось достаточного количества неправильных ответов, question: '" + recFactQuestion.getValue("factValue") + "', answer: '" + valueTrue + "'")
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
     * Подбирает неправильные варинаты для правильного, стараясь подобрать похожие по написанию
     * @param valueTrue
     * @param answerFalseValues
     * @return
     */
    Set<String> prepareValuesFalse(String valueTrue, Collection<String> answerFalseValues) {
        // Передали снаружи - их и вернем
        if (answerFalseValues != null) {
            return answerFalseValues
        }

        //
        Set<String> valuesFalseSet = new HashSet<>()


        // --- Выберем сами среди похожих слов, опираясь на таблицу WordDistance

        // Язык слова
        String lang
        if (UtWord.isAlphasEng(valueTrue)) {
            lang = "eng"
        } else if (UtWord.isAlphasRus(valueTrue)) {
            lang = "rus"
        } else {
            lang = "kaz"
        }

        //
        StoreRecord recWordDistance = mdb.loadQueryRecord(
                "select * from WordDistance where word = :word and lang = :lang",
                [word: valueTrue, lang: lang],
                false)
        if (recWordDistance != null) {
            String matchesJson = new String(recWordDistance.getValue("matches"), "utf-8")
            Map matchesMap = (Map) UtJson.fromJson(matchesJson)
            valuesFalseSet.addAll(matchesMap.keySet())
        }


        // --- Если не хватает похожих - добъем просто словами

        // Получаем список всех слов
        WordCacheService wordService = mdb.getModel().bean(WordCacheService)
        Store stFactTranslate = wordService.getStFactTranslate()

        // Добъем просто словами
        for (int n = valuesFalseSet.size(); n < VALUES_FALSE_MAX_COUNT; n++) {
            int p = rnd.num(0, stFactTranslate.size() - 1)
            StoreRecord rec = stFactTranslate.get(p)
            valuesFalseSet.add(rec.getString("factValue"))
        }


        //
        return valuesFalseSet
    }

    void setItemsForChoiceFalse(Collection<Long> idItems, long dataTypeAnswer) {
        if (idItems == null) {
            return
        }

        for (long idItem : idItems) {
            // Загружаем список фактов для "ответа"
            Fact_list list = mdb.create(Fact_list)
            Store stAnswer = list.loadFactsByDataType(idItem, dataTypeAnswer)

            // Каждое значение факта указанного типа будет вариантом выбора
            for (StoreRecord recAnswer : stAnswer) {
                String recAnswerValue = recAnswer.getValue("factValue")
                valuesForChoiceFalse.add(recAnswerValue)
            }
        }

    }


}
