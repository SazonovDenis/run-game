package run.game.dao.backstage

import groovy.transform.*
import jandcode.core.dao.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.dao.backstage.impl.*
import run.game.dao.ocr.*
import run.game.util.*

/**
 * Dao для поиска по словам, тексту или картинке.
 */
@TypeChecked
class Item_list extends RgmMdbUtils {


    @DaoMethod
    Store findItems(String text, long idPlan, Map tags) {
        // --- Подготовим аргументы
        List<TextPosition> textPositions = new ArrayList<>()
        textPositions.add(new TextPosition(text))

        // --- Поиск слов среди введенного
        Item_find finder = mdb.create(Item_find)
        Map<Long, String> tagsToFind = prepareParamsTags(tags)
        Store stItem = finder.collectItems(textPositions, tagsToFind, null)

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


        // --- Извлекаем из результатов OCR позиции слов (общий список)
        List<TextPosition> textPositions = new ArrayList<>()
        convertOcrResultToTextPositions(stParsedText, textPositions)


        // --- Поиск слов среди введенного
        Item_find finder = mdb.create(Item_find)
        Map tagsToFind = prepareParamsTags(tags)
        List<TextPosition> textPositionsRes = new ArrayList<>()
        Store stItem = finder.collectItems(textPositions, tagsToFind, textPositionsRes)


        // --- Заполнение свойств найденных слов
        Store stFact = makeFactList(stItem, idPlan)


        // --- Получим результат, где ключ - позиция
        // Объединим позиции с одинаковым текстом, объединим одинаковые позиции
        Map<Position, Set<TextItem>> mapPositions = new HashMap<>()
        StoreIndex idxItem = stItem.getIndex("value")

        for (TextPosition textPosition : textPositionsRes) {
            StoreRecord recFound = idxItem.get(textPosition.text)

            //
            String text = textPosition.text
            Long item = 0
            if (recFound != null) {
                item = recFound.getLong("id")
                text = recFound.getString("value")
            }

            //
            Set<TextItem> wordPositions = mapPositions.get(textPosition.position)
            if (wordPositions == null) {
                wordPositions = new HashSet<>()
                mapPositions.put(textPosition.position, wordPositions)
            }

            TextItem textItem = new TextItem(text, item)
            wordPositions.add(textItem)
        }


        // Формируем store с позициями и item
        Store stItemPositions = mdb.createStore("TextItemPosition")
        for (Position position : mapPositions.keySet()) {
            String text = null
            Collection<Long> items = new HashSet<>()
            Set<TextItem> positionTextItems = mapPositions.get(position)
            for (TextItem textItem : positionTextItems) {
                // Текст берем либо в первый раз, либо от элемента, где есть item
                if (text == null || textItem.item > 0) {
                    text = textItem.text
                }
                if (textItem.item > 0) {
                    items.add(textItem.item)
                }
            }

            //
            Map mapPosiotion = [
                    left  : position.left,
                    top   : position.top,
                    width : position.width,
                    height: position.height,
            ]

            //
            StoreRecord rec = stItemPositions.add()
            rec.setValue("position", mapPosiotion)
            rec.setValue("text", text)
            rec.setValue("items", items)
        }


        // ---
        DataBox res = new DataBox()
        res.put("facts", stFact)
        res.put("positions", stItemPositions)
        return res
    }


    // region Внутренние методы


    private Map<Long, String> prepareParamsTags(Map tags) {
        if (tags == null || tags.size() == 0) {
            return null
        } else if (tags.get("kaz") != null) {
            return [
                    (RgmDbConst.TagType_word_lang)               : RgmDbConst.Tag_word_lang_kaz,
                    (RgmDbConst.TagType_word_translate_direction): RgmDbConst.Tag_word_translate_direction_kaz_rus,
            ] as Map<Long, String>
        } else if (tags.get("eng") != null) {
            return [
                    (RgmDbConst.TagType_word_lang)               : RgmDbConst.Tag_word_lang_eng,
                    (RgmDbConst.TagType_word_translate_direction): RgmDbConst.Tag_word_translate_direction_eng_rus,
            ] as Map<Long, String>
        } else {
            return null
        }

    }


    /**
     * Перобразует данные от Tesseract (в tsv формате) в список
     *
     * @param stText данные от Tesseract
     * @param textPositions спискок слов их позиций
     */
    private void convertOcrResultToTextPositions(Store stText, List<TextPosition> textPositions) {
        for (int posStText = 0; posStText < stText.size(); posStText++) {
            StoreRecord rec = stText.get(posStText)

            //
            int conf = rec.getInt("conf")

            //
            String text = rec.getString("text")
            text = text.trim()

            //
            if (conf == 0 || text.length() == 0) {
                continue
            }

            //
            TextPosition textPosition = createTextPosition(rec)
            textPositions.add(textPosition)
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
                    StoreRecord recFactRes = stFactRes.add(recFact)
                    recFactRes.setValue("tag", recItem.getValue("tag"))
                }
            }
        }

        //
        return stFactRes
    }


    // endregion


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
    Fact_Spelling.factType factTypeSpelling,
    Fact_Spelling.value valueSpelling,
    
    Fact_Translate.id factAnswer, 
    Fact_Translate.factType factTypeTranslate,
    Fact_Translate.value valueTranslate,
    
    UsrFact.isHidden,
    UsrFact.isKnownGood,
    UsrFact.isKnownBad,
    (case when PlanFact.id is null then 0 else 1 end) isInPlan,
    
    coalesce(Cube_UsrFact.ratingTask, 0) ratingTask,
    coalesce(Cube_UsrFact.ratingQuickness, 0) ratingQuickness

from    
    Item
    -- Факт типа FactType_word_spelling
    join Fact Fact_Spelling on (
        Item.id = Fact_Spelling.item and
        Fact_Spelling.factType = ${RgmDbConst.FactType_word_spelling}
    )
    -- Факт типа FactType_word_translate 
    join Fact Fact_Translate on (
        Item.id = Fact_Translate.item and
        Fact_Translate.factType = ${RgmDbConst.FactType_word_translate}
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
    Fact_Spelling.factType factTypeSpelling,
    Fact_Spelling.value valueSpelling,
    
    Fact_Translate.id factAnswer, 
    Fact_Translate.factType factTypeTranslate,
    Fact_Translate.value valueTranslate,
    
    UsrFact.isHidden,
    UsrFact.isKnownGood,
    UsrFact.isKnownBad,
    (case when PlanFact.id is null then 0 else 1 end) isInPlan,
    
    coalesce(Cube_UsrFact.ratingTask, 0) ratingTask,
    coalesce(Cube_UsrFact.ratingQuickness, 0) ratingQuickness

from    
    Item
    -- Факт типа FactType_word_spelling
    join Fact Fact_Spelling on (
        Item.id = Fact_Spelling.item and
        Fact_Spelling.factType = ${RgmDbConst.FactType_word_spelling}
    )
    -- Факт типа FactType_word_translate 
    join Fact Fact_Translate on (
        Item.id = Fact_Translate.item and
        Fact_Translate.factType = ${RgmDbConst.FactType_word_translate}
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


    TextPosition createTextPosition(StoreRecord rec) {
        PagePosition pagePosition = new PagePosition(
                rec.getInt("par_num"),
                rec.getInt("line_num"),
                rec.getInt("word_num"),
        )
        Position position = new Position(
                rec.getInt("left"),
                rec.getInt("top"),
                rec.getInt("width"),
                rec.getInt("height"),
        )
        TextPosition textPosition = new TextPosition(
                rec.getString("text"),
                position,
                pagePosition,
        )
        return textPosition
    }

}
