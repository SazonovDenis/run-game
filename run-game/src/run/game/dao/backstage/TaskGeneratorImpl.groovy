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

public class TaskGeneratorImpl extends RgmMdbUtils implements TaskGenerator {

    Rnd rnd

    int OPTIONS_COUNT = 6
    int VALUES_FALSE_MAX_COUNT = 25

    Map<Long, String> FactType_CODE = [
            (RgmDbConst.FactType_word_spelling) : "word_spelling",
            (RgmDbConst.FactType_word_translate): "word_translate",
            (RgmDbConst.FactType_word_sound)    : "word_sound",
            (RgmDbConst.FactType_word_picture)  : "word_picture",
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
        Collection tagTypes = [RgmDbConst.TagType_word_lang, RgmDbConst.TagType_translate_direction]
        StoreRecord recFactQuestion = list.loadFactWithTags(factQuestion, tagTypes)
        StoreRecord recFactAnswer = list.loadFactWithTags(factAnswer, tagTypes)
        //
        long idItem = recFactQuestion.getLong("item")
        long idItemAnswer = recFactAnswer.getLong("item")
        long factTypeQuestion = recFactQuestion.getLong("factType")
        long factTypeAnswer = recFactAnswer.getLong("factType")
        String valueTrue = recFactAnswer.getValue("factValue")


        // Защита от дурака
        if (idItem != idItemAnswer) {
            println()
            println("recFactQuestion")
            mdb.outTable(recFactQuestion)
            println()
            println("recFactAnswer")
            mdb.outTable(recFactAnswer)
            throw new XError("idItem != idItemAnswer")
        }


        // ---
        // Генерим варианты ответов


        // Подберем неправильные варианты
        Map valueTrueTag = recFactAnswer.getValue("tag")
        Set<String> valuesFalseSet = prepareValuesFalse(valueTrue, valueTrueTag, answerFalseValues)


        // Фильтруем неправильные варианты
        String[] valueTrueWords = UtWord.filterAndSplitWord(valueTrue)

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
                    FactType_CODE[factTypeQuestion],
                    FactType_CODE[factTypeAnswer],
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
        Store stAnswer = list.loadBy_item_factType(idItem, factTypeAnswer)
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
                recOption.setValue("factType", recFactAnswer.getValue("factType"))
                recOption.setValue("value", valueTrue)
            } else {
                // Неправильные варианты
                int falseOptionIndex = falseOntionsIndexes[i]
                String valueFalse = valuesFalseArr[falseOptionIndex]
                //
                recOption.setValue("factType", recFactAnswer.getValue("factType"))
                recOption.setValue("value", valueFalse)
            }
        }


        // ---
        // Формируем stTaskQuestion: основной вопрос
        Store stTaskQuestion = mdb.createStore("TaskQuestion")


        // Формируем основной вопрос
        StoreRecord recTaskQuestion = stTaskQuestion.add()
        recTaskQuestion.setValue("factType", recFactQuestion.getValue("factType"))
        recTaskQuestion.setValue("value", recFactQuestion.getValue("factValue"))


        // Формируем дополнительную информацию
        if (factTypeQuestion != RgmDbConst.FactType_word_sound) {
            // Загружаем факты "звук"
            Store stFact = list.loadBy_item_factType(idItem, RgmDbConst.FactType_word_sound)
            // Выбираем факт "звук"
            if (stFact.size() != 0) {
                int idx = rnd.num(0, stFact.size() - 1)
                StoreRecord recFact = stFact.get(idx)
                recTaskQuestion = stTaskQuestion.add()
                recTaskQuestion.setValue("factType", recFact.getValue("factType"))
                recTaskQuestion.setValue("value", recFact.getValue("factValue"))

            }
        }
        if (factTypeQuestion != RgmDbConst.FactType_word_spelling) {
            // Загружаем факты "написание"
            Store stFact = list.loadBy_item_factType(idItem, RgmDbConst.FactType_word_spelling)
            // Выбираем факт "написание"
            if (stFact.size() != 0) {
                int idx = rnd.num(0, stFact.size() - 1)
                StoreRecord recFact = stFact.get(idx)
                recTaskQuestion = stTaskQuestion.add()
                recTaskQuestion.setValue("factType", recFact.getValue("factType"))
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


    /**
     * Подбирает неправильные варинаты для правильного, стараясь подобрать похожие по написанию
     * @param valueTrue
     * @param answerFalseValues
     * @return
     */
    Set<String> prepareValuesFalse(String valueTrue, Map valueTrueTag, Collection<String> answerFalseValues) {
        // Передали снаружи - их и вернем
        if (answerFalseValues != null) {
            return answerFalseValues
        }

        //
        Set<String> valuesFalseSet = new HashSet<>()


        // --- Выберем сами среди похожих слов, опираясь на таблицу WordDistance
        String valueTrueLang = UtTag.getLangByTag(valueTrueTag)
        StoreRecord recWordDistance = mdb.loadQueryRecord(
                "select * from WordDistance where word = :word and lang = :lang",
                [word: valueTrue, lang: valueTrueLang],
                false)
        if (recWordDistance != null) {
            String matchesJson = new String(recWordDistance.getValue("matches"), "utf-8")
            Map matchesMap = (Map) UtJson.fromJson(matchesJson)
            valuesFalseSet.addAll(matchesMap.keySet())
        }


        // --- Если не хватает похожих - добъем просто словами

        // Получаем список всех слов
        WordCacheService wordService = mdb.getModel().bean(WordCacheService)
        Store stFact = wordService.getStFact()

        // Выбираем из всех слов нужное количество.
        // Делаем большое, но конечное число попыток найти слова на нужном языке в списке всех слов.
        // Обеспечивает не зависание процесса поиска, если нужных слов не набирается.
        for (int n = 0; n < VALUES_FALSE_MAX_COUNT * 100; n++) {
            // Выбираем случайное слово
            int pos = rnd.num(0, stFact.size() - 1)
            StoreRecord rec = stFact.get(pos)
            Map valueFalseTag = rec.getValue("tag")
            String valueFalse = rec.getString("factValue")

            // Языки фактов совпадают?
            String valueFalseLang = UtTag.getLangByTag(valueFalseTag)
            if (!valueTrueLang.equals(valueFalseLang)) {
                continue
            }

            // Отсечем совсем уж неподходящий вариант - слова отличим от выражений
            int valueTrueSize = UtWord.filterAndSplitWord(valueTrue).size()
            int valueFalseSize = UtWord.filterAndSplitWord(valueFalse).size()
            // Вопрос - слово, ответ - одно слово (отличаем слова от выражений)
            if (valueTrueSize == 1 && valueFalseSize != 1) {
                continue
            }
            // Вопрос - два слова, ответ - два или три слова
            if (valueTrueSize == 2 && !(valueFalseSize == 2 || valueFalseSize == 3)) {
                continue
            }
            // Вопрос от ответа отличается не более чем на два слова (выражения примерно одинаковой длины)
            if (Math.abs(valueFalseSize - valueTrueSize) >= 3) {
                continue
            }

            // Добавляем слово в неправильные варианты
            valuesFalseSet.add(valueFalse)

            // Набрали нужное количество?
            if (valuesFalseSet.size() >= VALUES_FALSE_MAX_COUNT) {
                break
            }
        }


        //
        return valuesFalseSet
    }


}
