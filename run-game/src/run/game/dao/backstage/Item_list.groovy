package run.game.dao.backstage

import groovy.transform.*
import jandcode.commons.*
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
        ItemFinder finder = mdb.create(ItemFinder)
        Map<Long, String> tagsToFind = prepareParamsTags(tags)
        Store stItem = finder.collectItems(textPositions, tagsToFind, null)

        // --- Заполнение свойств найденных слов
        Store stFact = makeFactList(stItem, idPlan, tagsToFind)

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
        ItemFinder finder = mdb.create(ItemFinder)
        Map tagsToFind = prepareParamsTags(tags)
        List<TextPosition> textPositionsRes = new ArrayList<>()
        Store stItem = finder.collectItems(textPositions, tagsToFind, textPositionsRes)


        // --- Заполнение свойств найденных слов
        Store stFact = makeFactList(stItem, idPlan, tagsToFind)


        // --- Получим результат, где ключ - позиция
        // Объединим позиции с одинаковым текстом, объединим одинаковые позиции
        Map<Position, Set<TextItem>> mapPositions = new HashMap<>()
        StoreIndex idxItem = stItem.getIndex("factValue")

        for (TextPosition textPosition : textPositionsRes) {
            StoreRecord recFound = idxItem.get(textPosition.text)

            //
            String text = textPosition.text
            Long item = 0
            if (recFound != null) {
                item = recFound.getLong("id")
                text = recFound.getString("factValue")
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


    @DaoMethod
    DataBox loadItem(long idItem, long idPlan, Map<Long, String> tags) {
        // NB: Тут написано = tags.keySet().toList() а не просто = tags.keySet(),
        // т.к. почему-то в случае просто tags.keySet() у возавращенного объекта
        // метод add() выкидывает Exception. Не стал разбираться в чем дело.
        List<Long> tagTypes = tags.keySet().toList()
        tagTypes.add(RgmDbConst.TagType_dictionary)


        // --- Загружаем все факты для idItem
        Fact_list lst = mdb.create(Fact_list)
        Store stFactRaw = lst.loadBy_item(idItem, tagTypes)


        // --- Размажем список фактов в поля слова (recItem) и списку его переводов (stTasks)
        StoreRecord recItem = mdb.createStoreRecord("ItemInfo.item")
        Store stTasks = mdb.createStore("ItemInfo.task")

        // Сначала написание (чтобы получить factQuestion)
        long factQuestionId = 0
        for (StoreRecord recRaw : stFactRaw) {
            long factType = recRaw.getLong("factType")
            long factId = recRaw.getLong("id")

            // Данные по слову
            if (factType == RgmDbConst.FactType_word_spelling) {
                //
                setFlatRecord_value(recRaw, recItem)
                //
                factQuestionId = factId
            }
        }

        // Защита от дурака
        if (factQuestionId == 0) {
            throw new Exception("factQuestionId == 0")
        }

        // Переводы, примеры, транскрипция, звук
        for (StoreRecord recRaw : stFactRaw) {
            String dictionary = ""
            Map tag = recRaw.get("tag") as Map
            if (tag != null) {
                dictionary = tag.get(RgmDbConst.TagType_dictionary)
            }

            long factType = recRaw.getLong("factType")
            long factId = recRaw.getLong("id")


            // Данные по слову (из полного и краткого словарей)
            if (dictionary.equals(RgmDbConst.TagValue_dictionary_base) &&
                    factType == RgmDbConst.FactType_word_translate) {
                setFlatRecord_valueList(recRaw, recItem)
            }

            if (factType == RgmDbConst.FactType_word_transcription ||
                    factType == RgmDbConst.FactType_word_sound
            ) {
                setFlatRecord_value(recRaw, recItem)
            }

            if (factType == RgmDbConst.FactType_word_idiom) {
                setFlatRecord_valueList(recRaw, recItem)
            }


            // Данные по переводам слова (из полного словаря)
            if (!dictionary.equals(RgmDbConst.TagValue_dictionary_base) &&
                    factType == RgmDbConst.FactType_word_translate) {
                //
                StoreRecord recTask = stTasks.add()
                recTask.setValue("id", factId)
                recTask.setValue("factQuestion", factQuestionId)
                recTask.setValue("factAnswer", factId)
                //
                setFlatRecord_value(recRaw, recTask)
            }
        }

        // Примеры к переводам
        StoreIndex idxTasks = stTasks.getIndex("id")
        for (StoreRecord recRaw : stFactRaw) {
            long factType = recRaw.getLong("factType")
            long factOwnerId = recRaw.getLong("fact")

            //
            if (factType == RgmDbConst.FactType_word_example) {
                StoreRecord recTask = idxTasks.get(factOwnerId)
                setFlatRecord_valueList(recRaw, recTask)
            }
        }


        // --- Наполним данными о свойствах плана
        Set factIds = stFactRaw.getUniqueValues("id")
        long idUsr = getContextOrCurrentUsrId()
        Store stFactInfo = mdb.loadQuery(sqlPlanFactStatistic_Facts1(factIds), [usr: idUsr, plan: idPlan, factQuestion: factQuestionId])
        StoreIndex idxFactInfo = stFactInfo.getIndex("id")
        //
        for (StoreRecord recTask : stTasks) {
            StoreRecord recFactInfo = idxFactInfo.get(recTask.getLong("factAnswer"))
            recTask.setValue("isHidden", recFactInfo.getValue("isHidden"))
            recTask.setValue("isKnownGood", recFactInfo.getValue("isKnownGood"))
            recTask.setValue("isKnownBad", recFactInfo.getValue("isKnownBad"))
            recTask.setValue("isInPlan", recFactInfo.getValue("isInPlan"))
        }


        // ---
        DataBox res = new DataBox()
        res.put("item", recItem)
        res.put("tasks", stTasks)
        return res
    }

    // region Внутренние методы

    void setFlatRecord_value(StoreRecord recFact, StoreRecord recRes) {
        long factType = recFact.getLong("factType")
        String factValue = recFact.getValue("factValue")

        //
        String destFieldName = FactDataLoader.factTypeFieldNames.get(factType)

        // Определить поле по factType не получилось?
        if (UtCnv.isEmpty(destFieldName)) {
            return
        }

        // Если поле в источнике не заполнено - не заполняем
        if (UtCnv.isEmpty(factValue)) {
            return
        }

        // Если поле в получателе уже заполнено - не заполняем
        if (!recRes.isValueNull(destFieldName)) {
            return
        }

        // Заполняем
        recRes.setValue(destFieldName, factValue)
    }

    void setFlatRecord_valueList(StoreRecord recFact, StoreRecord recRes) {
        long factType = recFact.getLong("factType")
        String factValue = recFact.getValue("factValue")
        String destFieldName = FactDataLoader.factTypeFieldNames.get(factType)

        // Определить поле по factType не получилось?
        if (UtCnv.isEmpty(destFieldName)) {
            return
        }

        // Если поле в источнике не заполнено - не заполняем
        if (UtCnv.isEmpty(factValue)) {
            return
        }

        // Создаем значение типа List
        List valueRes = recRes.getValue(destFieldName) as List
        if (valueRes == null) {
            valueRes = new ArrayList()
            recRes.setValue(destFieldName, valueRes)
        }

        // Заполняем
        valueRes.add(factValue)
    }

    private Map<Long, String> prepareParamsTags(Map tags) {
        if (tags == null || tags.size() == 0) {
            return [:]
        } else if (tags.get("kaz") != null) {
            return [
                    (RgmDbConst.TagType_word_lang)          : RgmDbConst.TagValue_word_lang_kaz,
                    (RgmDbConst.TagType_translate_direction): RgmDbConst.TagValue_translate_direction_kaz_rus,
            ] as Map<Long, String>
        } else if (tags.get("eng") != null) {
            return [
                    (RgmDbConst.TagType_word_lang)          : RgmDbConst.TagValue_word_lang_eng,
                    (RgmDbConst.TagType_translate_direction): RgmDbConst.TagValue_translate_direction_eng_rus,
            ] as Map<Long, String>
        } else {
            return [:]
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


    private Store makeFactList(Store stItem, long idPlan, Map<Long, String> tags) {
        // --- Превратим список Item в список пар фактов (сгенерим комбинации переводов)
        Store stFact = loadFactList(stItem, idPlan, tags)

        // --- Дополним факты в плане "богатыми" данными для вопроса и ответа
        FactDataLoader ldr = mdb.create(FactDataLoader)
        ldr.fillFactBody(stFact)

        // Уберем полные (dictionary=full) варианты перевода,
        // если найдены базовые (dictionary=base) переводы
        filterFullIfBaseExists(stFact)

        // --- Обеспечим порядок, как в исходном тексте
        Store stFactOrderd = orderByItem(stItem, stFact)

        //
        return stFactOrderd
    }

    private void filterFullIfBaseExists(Store stFact) {
        // Соберем те item-ы, у фактов которых есть базовый перевод
        Set<String> itemsHasBase = new HashSet<>()
        for (StoreRecord rec : stFact) {
            //
            Map tag = (Map) rec.getValue("tag")
            // todo ПОТОМ long keyValue =rec.getLong("item")
            String keyValue = (rec.getValue("question") as Map).get("valueSpelling")
            //
            if (tag != null && tag.get(RgmDbConst.TagType_dictionary) == RgmDbConst.TagValue_dictionary_base) {
                itemsHasBase.add(keyValue)
            }
        }

        // Уберем факты с расширенным переводом у тех item, у которых нашли базовый перевод
        Set<String> itemsWasFullDeleted = new HashSet<>()
        for (int i = stFact.size() - 1; i >= 0; i--) {
            StoreRecord rec = stFact.get(i)
            //
            Map tag = (Map) rec.getValue("tag")
            // todo ПОТОМ long keyValue =rec.getLong("item")
            String keyValue = (rec.getValue("question") as Map).get("valueSpelling")
            //
            if (itemsHasBase.contains(keyValue) && tag != null && tag.get(RgmDbConst.TagType_dictionary) == RgmDbConst.TagValue_dictionary_full) {
                stFact.remove(i)
                //
                itemsWasFullDeleted.add(keyValue)
            }
        }

        // Для тех item, у которых удалили расширенный перевод выставим флаг "wasFullDeleted"
        for (StoreRecord rec : stFact) {
            //
            Map tag = (Map) rec.getValue("tag")
            // todo ПОТОМ long keyValue =rec.getLong("item")
            String keyValue = (rec.getValue("question") as Map).get("valueSpelling")
            //
            if (itemsWasFullDeleted.contains(keyValue)) {
                tag.put("hasDictionaryFull", true)
            }
        }

    }

    /**
     * @return Список с информацией пользователя, наличие/отсутствие в указанном плане, статистикой.
     */
    private Store loadFactList(Store stItem, long idPlan, Map<Long, String> tags) {
        Store stPlanFact = mdb.createStore("PlanFact.list")

        // --- Соберем отдельно item, отдельно конкретные факты
        Set itemIds = new HashSet()
        Set factIds = new HashSet()
        for (StoreRecord recItem : stItem) {
            if (recItem.getLong("fact") == 0) {
                itemIds.add(recItem.getLong("id"))
            } else {
                factIds.add(recItem.getLong("fact"))
            }
        }


        // --- Загрузка списка itemIds

        // Формируем пары фактов spelling -> translate, т.е. все варианты перевода
        long idUsr = getContextOrCurrentUsrId()
        Store stFact = mdb.createStore("PlanFact.list")
        mdb.loadQuery(stFact, sqlPlanFactStatistic_Items(itemIds), [usr: idUsr, plan: idPlan])

        // --- Загрузим тэги фактов

        // Загрузим заказанные
        // NB: Тут написано = tags.keySet().toList() а не просто = tags.keySet(),
        // т.к. почему-то в случае просто tags.keySet() у возавращенного объекта
        // метод add() выкидывает Exception. Не стал разбираться в чем дело.
        List<Long> tagTypes = tags.keySet().toList()

        // Также безусловно загрузим TagType:dictionary
        tagTypes.add(RgmDbConst.TagType_dictionary)

        // Загрузим тэги
        Fact_list factList = mdb.create(Fact_list)
        Store stTags = factList.loadTagsByIds(stFact.getUniqueValues("factAnswer"), tagTypes)

        // Распределим тэги
        factList.spreadTags(stTags, stFact)


        // --- Фильтруем все варианты перевода, оставляем только подхоящие по тэгам
        UtTag.cleanStoreByTags(stFact, tags)

        //
        stFact.copyTo(stPlanFact)


        // --- Загрузка списка factIds
        stFact.clear()
        mdb.loadQuery(stFact, sqlPlanFactStatistic_Facts(factIds), [usr: idUsr, plan: idPlan])

        //
        stFact.copyTo(stPlanFact)


        // ---
        return stPlanFact
    }

    private Store orderByItem(Store stItem, Store stFact) {
        Store stFactRes = mdb.createStore("PlanFact.list")

        //
        Map<Object, List<StoreRecord>> factsByItem = StoreUtils.collectGroupBy_records(stFact, ["item"])

        //
        for (StoreRecord recItem : stItem) {
            String item = recItem.getString("id")
            List<StoreRecord> lstItemFacts = factsByItem.get(item)
            if (lstItemFacts != null) {
                for (StoreRecord recFact : lstItemFacts) {
                    StoreRecord recFactRes = stFactRes.add(recFact)
                    Map tag = new HashMap()
                    if (recFactRes.getValue("tag") != null) {
                        tag.putAll(recFactRes.getValue("tag") as Map)
                    }
                    if (recItem.getValue("tag") != null) {
                        tag.putAll(recItem.getValue("tag") as Map)
                    }
                    if (tag.size() != 0) {
                        recFactRes.setValue("tag", tag)
                    }
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
-- Формируем пары фактов spelling -> translate, т.е. все варианты перевода, для сущностей по списку items
-- Загрузим информацию об этих фактах
select 
    Fact_Translate.id, 
    
    Fact_Spelling.item,
    
    Fact_Spelling.id factQuestion,
    Fact_Spelling.factType factTypeSpelling,
    Fact_Spelling.factValue valueSpelling,
    
    Fact_Translate.id factAnswer, 
    Fact_Translate.factType factTypeTranslate,
    Fact_Translate.factValue valueTranslate,
    
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
    Fact_Spelling.item,
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
-- Загрузим информацию о фактах по списку facts
select 
    Fact_Translate.id, 

    Fact_Spelling.item,
    
    Fact_Spelling.id factQuestion,
    Fact_Spelling.factType factTypeSpelling,
    Fact_Spelling.factValue valueSpelling,
    
    Fact_Translate.id factAnswer, 
    Fact_Translate.factType factTypeTranslate,
    Fact_Translate.factValue valueTranslate,
    
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
    Fact_Spelling.item,
    Fact_Spelling.id,    
    Fact_Translate.id    
"""
    }

    protected String sqlPlanFactStatistic_Facts1(Collection facts) {
        String factsStr = "0"
        if (facts.size() > 0) {
            factsStr = facts.join(",")
        }

        return """ 
-- Загрузим информацию о фактах по списку facts
select 
    Fact.id, 
    Fact.item,
    
    UsrFact.isHidden,
    UsrFact.isKnownGood,
    UsrFact.isKnownBad,
    (case when PlanFact.id is null then 0 else 1 end) isInPlan,
    
    coalesce(Cube_UsrFact.ratingTask, 0) ratingTask,
    coalesce(Cube_UsrFact.ratingQuickness, 0) ratingQuickness

from    
    Fact
    -- Информация о наличии пары фактов в указанном плане
    left join PlanFact on (
        PlanFact.plan = :plan and
        PlanFact.factQuestion = :factQuestion and 
        PlanFact.factAnswer = Fact.id 
    )
    -- Информация пользователя о паре фактов
    left join UsrFact on (
        UsrFact.usr = :usr and
        UsrFact.factQuestion = :factQuestion and 
        UsrFact.factAnswer = Fact.id 
    )
    -- Статистическая информация о паре фактов
    left join Cube_UsrFact on (
        Cube_UsrFact.usr = :usr and 
        Cube_UsrFact.factQuestion = :factQuestion and 
        Cube_UsrFact.factAnswer = Fact.id
    )
    
where
    Fact.id in (${factsStr})   
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
