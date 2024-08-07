package run.game.dao.backstage

import jandcode.commons.*
import jandcode.core.dao.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.dao.ocr.*
import run.game.util.*

/**
 * Dao для поиска по словам, тексту или картинке.
 */
class Item_list extends RgmMdbUtils {


    @DaoMethod
    Store findItems(String text, long idPlan, Map tags) {
        // --- Подготовим аргументы
        Map<String, Set> textItems = new LinkedHashMap<>()
        textItems.put(text, null)

        // --- Поиск слов среди введенного
        Item_find finder = mdb.create(Item_find)
        Map tagsToFind = prepareParamsTags(tags)
        Store stItem = finder.collectItems(textItems, tagsToFind, null)

        // --- Заполнение свойств найденных слов
        Store stFact = makeFactList(stItem, idPlan)

        // ---
        return stFact
    }


    @DaoMethod
    DataBox findStill(String imgBase64, long idPlan, Map tags) {
        // Разберем картинку
        Ocr ocr = mdb.create(Ocr)
        Store stParsedText = ocr.parseStillMarkup(imgBase64)


        // --- Раскладываем результаты OCR на части

        // Слова и их позиции
        Map<String, Set> textItems = new LinkedHashMap<>()
        // Позиции слов (общий список)
        List textPositions = new ArrayList<>()
        // Раскладываем
        splitOcrResult(stParsedText, textItems, textPositions)


        // --- Поиск слов среди введенного
        Map<String, Set> textItemsFiltered = new HashMap<>()
        Item_find finder = mdb.create(Item_find)
        Map tagsToFind = prepareParamsTags(tags)
        Store stItem = finder.collectItems(textItems, tagsToFind, textItemsFiltered)


        // --- Заполнение свойств найденных слов
        Store stFact = makeFactList(stItem, idPlan)


        // --- Позиции
        List wordList = textItemsFiltered.keySet().toList()
        Map<Object, List<StoreRecord>> mapItems = StoreUtils.collectGroupBy_records(stItem, "value")
        //
        Store stItemPositions = mdb.createStore("Tesseract.items")
        for (String word : wordList) {
            List recs = mapItems.get(word)
            long item = 0
            if (recs != null) {
                item = recs.get(0).getLong("id")
            }
            StoreRecord rec = stItemPositions.add()
            rec.setValue("item", item)
            rec.setValue("itemValue", word)
            rec.setValue("positions", textItemsFiltered.get(word))
        }


        // ---
        DataBox res = new DataBox()
        res.put("facts", stFact)
        res.put("positions", stItemPositions)
        return res
    }


    // region Внутренние методы


    private Map prepareParamsTags(Map tags) {
        if (tags.size() == 0) {
            return null
        } else if (tags.get("kaz") != null) {
            tags = [
                    (RgmDbConst.TagType_word_lang)               : RgmDbConst.Tag_word_lang_kaz,
                    (RgmDbConst.TagType_word_translate_direction): RgmDbConst.Tag_word_translate_direction_kaz_rus,
            ]
            return tags
        } else if (tags.get("eng") != null) {
            tags = [
                    (RgmDbConst.TagType_word_lang)               : RgmDbConst.Tag_word_lang_eng,
                    (RgmDbConst.TagType_word_translate_direction): RgmDbConst.Tag_word_translate_direction_eng_rus,
            ]
            return tags
        } else {
            return null
        }

    }


    /**
     *
     * @param stText данные от Tesseract
     * @param textItems разобранные слова, ключ: слово, значение: список позиций, где встретился
     * @param textItemsPosition отдельно спискок позиций
     */
    private void splitOcrResult(Store stText, Map<String, Set> textItems, List textItemsPosition) {
        for (int i = 0; i < stText.size(); i++) {
            StoreRecord rec = stText.get(i)

            //
            int conf = rec.getInt("conf")

            //
            String text = rec.getString("text")
            text = text.trim()

            //
            if (conf == 0 || text.length() == 0) {
                continue
            }


            // Слияние по переносам
            Map textPositionNext = null
            if (i < stText.size() - 2) {
                StoreRecord recNext = stText.get(i + 2)
                int line_num = rec.getInt("line_num")
                int line_numNext = recNext.getInt("line_num")
                String textNext = recNext.getString("text")
                textNext = textNext.trim()
                //
                if (text.endsWith("-") && (line_numNext - line_num) == 1) {
                    text = text.substring(0, text.length() - 1) + textNext

                    textPositionNext = [
                            text  : text,
                            left  : recNext.getInt("left"),
                            top   : recNext.getInt("top"),
                            width : recNext.getInt("width"),
                            height: recNext.getInt("height"),
                    ]

                    //
                    i = i + 2
                }
            }

            // Формируем позицию
            if (textPositionNext != null) {
                textItemsPosition.add(textPositionNext)
            }

            //
            Map textPosition = [
                    text  : text,
                    left  : rec.getInt("left"),
                    top   : rec.getInt("top"),
                    width : rec.getInt("width"),
                    height: rec.getInt("height"),
            ]
            textItemsPosition.add(textPosition)

            //
            Set textItemPositions = textItems.get(text)
            if (textItemPositions == null) {
                textItemPositions = new HashSet()
                textItems.put(text, textItemPositions)
            }
            textItemPositions.add(textPosition)
            //
            if (textPositionNext != null) {
                textItemPositions.add(textPositionNext)
            }
        }
    }

    private Store makeFactList(Store stItem, long idPlan) {
        // --- Превратим список Item в список пар фактов (сгенерим комбинации)
        Store stFact = loadFactList(stItem, idPlan)

        // --- Дополним факты в плане "богатыми" данными для вопроса и ответа
        FactDataLoader ldr = mdb.create(FactDataLoader)
        ldr.fillFactBody(stFact)

        // --- Обеспечим порядок, как в исходном тексте
        Store stFactOrderd = makeOrdered(stItem, stFact)

        //
        return stFactOrderd
    }

    private Store loadFactList(Store stItem, long idPlan) {
        Store stPlanFact = mdb.createStore("PlanFact.list")

        // Соберем отдельно item, отдельно конкретные факты
        Set itemIds = new HashSet()
        Set factIds = new HashSet()
        for (StoreRecord recItem : stItem) {
            if (recItem.getLong("fact") == 0) {
                itemIds.add(recItem.getLong("id"))
            } else {
                factIds.add(recItem.getLong("fact"))
            }
        }

        // Поиск Item: формируем пары фактов spelling -> translate, т.е. все варианты перевода (для списка itemIds).
        // Список с информацией пользователя, наличие/отсутствие в указанном плане, статистикой.
        long idUsr = getContextOrCurrentUsrId()
        mdb.loadQuery(stPlanFact, sqlPlanFactStatistic_Items(itemIds), [usr: idUsr, plan: idPlan])

        // Поиск Fact
        mdb.loadQuery(stPlanFact, sqlPlanFactStatistic_Facts(factIds), [usr: idUsr, plan: idPlan])

        //
        return stPlanFact
    }

    private Store makeOrdered(Store stItem, Store stFact) {
        Store stFactRes = mdb.createStore("PlanFact.list")

        //
        Map<Object, List<StoreRecord>> factsByItems = StoreUtils.collectGroupBy_records(stFact, ["item"])
        for (StoreRecord recItem : stItem) {
            String item = recItem.getString("id")
            List<StoreRecord> lstItemFacts = factsByItems.get(item)
            if (lstItemFacts != null) {
                for (StoreRecord recFact : lstItemFacts) {
                    stFactRes.add(recFact)
                }
            }
        }

        //
        return stFactRes
    }


    // endregion

    /**
     * Ищем Item или Fact по фрагменту написания,
     * среди наших словарных слов
     *
     * @param itemText фрагмент для поиска
     * @return Store структуры "Item.find"
     */
/*
    Store find(String text) {
        text = text.toLowerCase().trim()

        if (text.length() == 0) {
            throw new XError("Строка для поиска не указана")
        }

        // Текст с разделителями?
        Collection<String> lst = UtWord.filterAndSplitWord(text)

        //
        if (lst.size() > 1) {
            // Ищем сразу много слов по полному написанию
            return findText(lst)
        } else {
            // Ищем одно слово по слову или фрагменту
            return findWord(text)
        }
    }
*/


    // region sql


    protected String sqlPlanFactStatistic_Items(Collection items) {
        String itemsStr = "0"
        if (items.size() > 0) {
            itemsStr = items.join(",")
        }

        return """ 
-- Формируем пары фактов spelling -> translate, т.е. все варианты перевода, для сущностей по списку
select 
    Item.id item,
    
    Fact_Spelling.id factQuestion,
    Fact_Spelling.dataType dataTypeSpelling,
    Fact_Spelling.value valueSpelling,
    
    Fact_Translate.id factAnswer, 
    Fact_Translate.dataType dataTypeTranslate,
    Fact_Translate.value valueTranslate,
    
    UsrFact.isHidden,
    UsrFact.isKnownGood,
    UsrFact.isKnownBad,
    (case when PlanFact.id is null then 0 else 1 end) isInPlan,
    
    coalesce(Cube_UsrFact.ratingTask, 0) ratingTask,
    coalesce(Cube_UsrFact.ratingQuickness, 0) ratingQuickness

from    
    Item
    -- Факт типа DataType_word_spelling
    join Fact Fact_Spelling on (
        Item.id = Fact_Spelling.item and
        Fact_Spelling.dataType = ${RgmDbConst.DataType_word_spelling}
    )
    -- Факт типа DataType_word_translate 
    join Fact Fact_Translate on (
        Item.id = Fact_Translate.item and
        Fact_Translate.dataType = ${RgmDbConst.DataType_word_translate}
    )
    -- Информация о наличии пары фактов в указанном плане
    left join PlanFact on (
        PlanFact.plan = :plan and
        PlanFact.factQuestion = Fact_Spelling.id and 
        PlanFact.factAnswer = Fact_Translate.id 
    )
    -- Информация пользователя о паре фактов
    left join UsrFact on (
        UsrFact.usr = :usr and
        UsrFact.factQuestion = Fact_Spelling.id and 
        UsrFact.factAnswer = Fact_Translate.id 
    )
    -- Статистическая информация о паре фактов
    left join Cube_UsrFact on (
        Cube_UsrFact.usr = :usr and 
        Cube_UsrFact.factQuestion = Fact_Spelling.id and 
        Cube_UsrFact.factAnswer = Fact_Translate.id
    )
    
where
    Item.id in (${itemsStr})   

order by
    Item.id,
    Fact_Spelling.id,    
    Fact_Translate.id    
"""
    }

    protected String sqlPlanFactStatistic_Facts(Collection facts) {
        String factsStr = "0"
        if (facts.size() > 0) {
            factsStr = facts.join(",")
        }

        return """ 
-- Формируем пары фактов spelling -> translate, т.е. все варианты перевода, для сущностей по списку
select 
    Item.id item,
    
    Fact_Spelling.id factQuestion,
    Fact_Spelling.dataType dataTypeSpelling,
    Fact_Spelling.value valueSpelling,
    
    Fact_Translate.id factAnswer, 
    Fact_Translate.dataType dataTypeTranslate,
    Fact_Translate.value valueTranslate,
    
    UsrFact.isHidden,
    UsrFact.isKnownGood,
    UsrFact.isKnownBad,
    (case when PlanFact.id is null then 0 else 1 end) isInPlan,
    
    coalesce(Cube_UsrFact.ratingTask, 0) ratingTask,
    coalesce(Cube_UsrFact.ratingQuickness, 0) ratingQuickness

from    
    Item
    -- Факт типа DataType_word_spelling
    join Fact Fact_Spelling on (
        Item.id = Fact_Spelling.item and
        Fact_Spelling.dataType = ${RgmDbConst.DataType_word_spelling}
    )
    -- Факт типа DataType_word_translate 
    join Fact Fact_Translate on (
        Item.id = Fact_Translate.item and
        Fact_Translate.dataType = ${RgmDbConst.DataType_word_translate}
    )
    -- Информация о наличии пары фактов в указанном плане
    left join PlanFact on (
        PlanFact.plan = :plan and
        PlanFact.factQuestion = Fact_Spelling.id and 
        PlanFact.factAnswer = Fact_Translate.id 
    )
    -- Информация пользователя о паре фактов
    left join UsrFact on (
        UsrFact.usr = :usr and
        UsrFact.factQuestion = Fact_Spelling.id and 
        UsrFact.factAnswer = Fact_Translate.id 
    )
    -- Статистическая информация о паре фактов
    left join Cube_UsrFact on (
        Cube_UsrFact.usr = :usr and 
        Cube_UsrFact.factQuestion = Fact_Spelling.id and 
        Cube_UsrFact.factAnswer = Fact_Translate.id
    )
    
where
    Fact_Translate.id in (${factsStr})   

order by
    Item.id,
    Fact_Spelling.id,    
    Fact_Translate.id    
"""
    }

    // endregion


}
