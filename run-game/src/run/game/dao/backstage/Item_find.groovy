package run.game.dao.backstage

import jandcode.core.dbm.mdb.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.model.service.*

/**
 * Поиск по словам или тексту.
 */
class Item_find extends BaseMdbUtils {


    public int MAX_COUNT_FOUND = 20
    public int MAX_COUNT_FOUND_ONE_TYPE = 10


    /**
     * Выполняет поиск элементов textItemsRaw в словарях.
     * Элементы пердварительно очищаются и преобразовываются.
     *
     * @param textItemsRaw Текстовые фрагменты. Каждый элемент ключом имеет текст,
     * значением - набор позиций в источнике, где этот элемент встретился
     * @param tags Фильтр по тэгам, например по языку
     * @param textItemsFiltered todo: нужен ли агрумент
     * @return Найденные слова: store со списком Item-ов и Fact-ов.
     */
    Store collectItems(Map<String, Set> textItemsRaw, Map tags, Map<String, Set> textItemsFiltered) {
        // --- Обработка найденного: фильтрация и чистка
        Map<String, Set> textItems = filterTextItems(textItemsRaw, (word, item) -> {
            return UtWord.filterAndSplitWord(word)
        })


        // --- Обработка найденного: стоп-слова

        // Получаем из кэша стоп-слова
        WordCacheService wordService = mdb.getModel().bean(WordCacheService)
        StoreIndex idxOcrStopWords = wordService.getIdxOcrStopWords()

        // Фильтруем по стоп-словам
        textItems = filterTextItems(textItems, (word, item) -> {
            List res = new ArrayList()
            if (idxOcrStopWords.get(word.toLowerCase()) == null) {
                res.add(word)
            }
            return res
        })


        // --- Поиск по точному совпадению

        //
        List<String> text = textItems.keySet().toList()


        // Ищем Items по точному совпадению с textItems
        Store stItem = findTextExact(text, tags)


        // --- Поиск того, что не нашлось: по точному совпадению, но с перобразованем словоформ

        // Проверим, какие слова не нашлись по непосредственному написанию
        StoreIndex idxItems = stItem.getIndex("value")
        Map<String, Set> textItemsNotFound = new LinkedHashMap<>()
        //
        for (String word : text) {
            if (idxItems.get(word) == null) {
                textItemsNotFound.put(word, textItems.get(word))
            }
        }

        // Если часть слов не нашлось -
        // выполняем для них преобразование словоформ и делаем вторую попытку
        Map<String, Set> textItemsTranformed = filterTextItems(textItemsNotFound, (word, item) -> {
            return transformWord(word)
        })

        if (textItemsTranformed.size() != 0) {
            List textTranformed = textItemsTranformed.keySet().toList()

            // Ищем Items по преобразованным словам
            Store stItemTransformed = findTextExact(textTranformed, tags)

            // Обновляем наши списки
            stItemTransformed.copyTo(stItem)
            //
            textItems.putAll(textItemsTranformed)
        }


        // --- Поиск по фрагменту

        // Если искали одно слово и нашили мало - то поищем ещё и по фрагменту
        if (text.size() == 1 && stItem.size() <= MAX_COUNT_FOUND) {
            // Ищем Items по фрагменту
            Set itemsFound = stItem.getUniqueValues("value")
            String word = text.get(0)
            Store stItemWord = findWordPart(word, tags)

            // Обновляем наши списки
            for (StoreRecord recItemWord : stItemWord) {
                String value = recItemWord.getString("value")
                if (!itemsFound.contains(value)) {
                    stItem.add(recItemWord)
                    itemsFound.add(value)
                }
            }
        }


        // ---
        if (textItemsFiltered != null) {
            textItemsFiltered.putAll(textItems)
        }


        // ---
        return stItem
    }


    /**
     * Ищем СПИСОК слов, каждый по ПОЛНОМУ написанию или переводу,
     * среди наших словарных слов.
     *
     * @param text список слов (очищенный)
     * @return Найденные слова: store со списком Item
     */
    Store findTextExact(Collection<String> text, Map tags) {
        Store stItem = mdb.createStore("Item.find")

        // Формируем пары из продряд идущих слов.
        // Так можно попытаться найти словарное слово, которое состоит из нескольких слов,
        // например: "go on", "он екі".
        Collection<String> itemsTextPairs = createPairs(text)
        text.addAll(itemsTextPairs)

        // Получаем из кэша spelling и translate для всех слов
        WordCacheService wordService = mdb.getModel().bean(WordCacheService)
        Map<Object, List<StoreRecord>> idxFacts = wordService.getIdxFacts()

        // Отберем среди text те слова, котрые есть в наших словарях
        Set<Long> setItems = new HashSet<>()
        for (String itemText : text) {
            List<StoreRecord> recs = idxFacts.get(itemText)
            if (recs != null) {
                for (StoreRecord rec : recs) {
                    long item = rec.getLong("item")

                    // Повторы не нужны
                    if (setItems.contains(item)) {
                        continue
                    }

                    // Фильтр по тэгам
                    boolean wasTagComparationTrue = true
                    Map factTag = rec.getValue("factTag")
                    if (factTag != null && tags != null && tags.size() != 0) {
                        wasTagComparationTrue = false
                        for (Map.Entry entry : tags.entrySet()) {
                            long tagKey = entry.getKey()
                            long tagValue = entry.getValue()
                            if (tagValue.equals(factTag.get(tagKey))) {
                                wasTagComparationTrue = true
                                break
                            }
                        }
                    }
                    if (!wasTagComparationTrue) {
                        continue
                    }

                    //
                    stItem.add([id: item, value: rec.getValue("itemValue")])
                    setItems.add(item)
                }
            }
        }

        //
        return stItem
    }


    /**
     * Ищем ОДНО слово, по ФРАГМЕНТУ написания или перевода,
     * среди наших словарных слов.
     *
     * @param word слово или его фрагмент
     * @return Найденные слова: store со списком Item
     */
    Store findWordPart(String word, Map tags) {
        Store stItem = mdb.createStore("Item.find")

        //
        int count
        Fact_list list = mdb.create(Fact_list)
        Set<Long> setItemsFound = new HashSet<>()


        // ---
        // Поиск

        Collection tagsValues = null
        if (tags != null) {
            tagsValues = tags.values()
        }

        // Поиск по атрибуту word_spelling
        Store stFactSpelling = list.findFactsByValueDataTypeByTags(word, RgmDbConst.DataType_word_spelling, tagsValues)
        stFactSpelling.sort("itemValue")


        // Поиск по атрибуту word_translate
        Store stFactTranslate = list.findFactsByValueDataTypeByTags(word, RgmDbConst.DataType_word_translate, tagsValues)
        stFactTranslate.sort("factValue")


        // ---
        // Перенос найденных слов в результат


        // В первую очередь перенесём слова, НАЧИНАЮЩИЕСЯ на фрагмент.

        // Результат по атрибуту word_spelling.
        count = 0
        for (StoreRecord rec : stFactSpelling) {
            long itemId = rec.getLong("item")
            String value = rec.getString("itemValue")

            // Сейчас - только слова НАЧИНАЮЩИЕСЯ на фрагмент
            if (!value.startsWith(word)) {
                continue
            }

            // Повторы не нужны
            if (setItemsFound.contains(itemId)) {
                continue
            }

            // Не более MAX_COUNT_FOUND_ONE_TYPE в результатах
            if (count > MAX_COUNT_FOUND_ONE_TYPE) {
                break
            }
            count++

            //
            stItem.add([id: itemId, value: value])
            setItemsFound.add(itemId)
        }


        // Результат по атрибуту word_translate.
        count = 0
        for (StoreRecord rec : stFactTranslate) {
            long item = rec.getLong("item")
            String factValue = rec.getString("factValue")
            String value = rec.getString("itemValue")

            // Сейчас - только слова НАЧИНАЮЩИЕСЯ на фрагмент
            if (!factValue.startsWith(word)) {
                continue
            }

            // Повторы не нужны
            if (setItemsFound.contains(item)) {
                continue
            }

            // Не более MAX_COUNT_FOUND_ONE_TYPE в результатах
            if (count > MAX_COUNT_FOUND_ONE_TYPE) {
                break
            }
            count++

            //
            stItem.add([id: item, value: value, fact: rec.getValue("id")])
            setItemsFound.add(item)
        }


        // Во вторую очередь слова СОДЕРЖАЩИЕ фрагмент.

        // Результат по атрибуту word_spelling.
        count = 0
        for (StoreRecord rec : stFactSpelling) {
            long item = rec.getLong("item")
            String value = rec.getString("itemValue")

            // Сейчас - только слова СОДЕРЖАЩИЕ фрагмент
            if (value.startsWith(word)) {
                continue
            }

            // Повторы не нужны
            if (setItemsFound.contains(item)) {
                continue
            }

            // Не более MAX_COUNT_FOUND_ONE_TYPE в результатах
            if (count > MAX_COUNT_FOUND_ONE_TYPE) {
                break
            }
            count++

            //
            stItem.add([id: item, value: rec.getValue("itemValue")])
            setItemsFound.add(item)
        }


        // Результат по атрибуту word_translate.
        count = 0
        for (StoreRecord rec : stFactTranslate) {
            long item = rec.getLong("item")
            String factValue = rec.getString("factValue")
            String value = rec.getString("itemValue")

            // Сейчас - только слова СОДЕРЖАЩИЕ фрагмент
            if (factValue.startsWith(word)) {
                continue
            }

            // Повторы не нужны
            if (setItemsFound.contains(item)) {
                continue
            }

            // Не более MAX_COUNT_FOUND_ONE_TYPE в результатах
            if (count > MAX_COUNT_FOUND_ONE_TYPE) {
                break
            }
            count++

            //
            stItem.add([id: item, value: value, fact: rec.getValue("id")])
            setItemsFound.add(item)
        }


        // ---
        return stItem
    }


    // Загрузим тэги для item
/*      // Загрузим тэги для item
        Store stItemTag = mdb.loadQuery(sqlItemTag(stItem.getUniqueValues("id")))
        Map<Object, List<StoreRecord>> mapItemsTag = StoreUtils.collectGroupBy_records(stItemTag, "item")

        // Заполним поле тэги (itemTag)
        for (StoreRecord recItem : stItem) {
            List<StoreRecord> lstRecItemTag = mapItemsTag.get(recItem.getLong("id"))
            if (lstRecItemTag != null) {
                // Превратим список тэгов в Map тегов
                Map<Long, String> mapItemTag = new HashMap<>()
                for (StoreRecord recItemTag : lstRecItemTag) {
                    mapItemTag.put(recItemTag.getLong("tagType"), recItemTag.getString("value"))
                }
                recItem.setValue("itemTag", mapItemTag)
            }
        }

        //
        return stItem
    }

    protected String sqlItemTag(Set ids) {
        String strIds
        if (ids.size() == 0) {
            strIds = "0"
        } else {
            strIds = ids.join(",")
        }
        return """
select
    ItemTag.item,
    Tag.*
from
    ItemTag
    join Tag on (ItemTag.tag = Tag.id)
where
    ItemTag.item in ( ${strIds} )
"""
    }
*/


    // region Внутренние утилиты


    interface IWordAnalyzer {
        Collection<String> analyzeText(String text, Set item)
    }


    /**
     * Обрабатывает каждый элемент из textItems с помощью обработчика wordAnalyzer.
     *
     * @param textItems список вхождений текстов, где ключ - текст, а значения - набор мест расположения текста на изображении
     * @param wordAnalyzer
     *
     * @return Новый список, может быть больше или меньше исходного
     */
    private Map<String, Set> filterTextItems(Map<String, Set<Map>> textItems, IWordAnalyzer wordAnalyzer) {
        Map<String, Set> textItemsRes = new LinkedHashMap<>()

        for (String itemText : textItems.keySet()) {
            Set textItemPositions = textItems.get(itemText)
            List wordsFiltered = wordAnalyzer.analyzeText(itemText, textItemPositions)
            for (String wordFiltered : wordsFiltered) {
                Set textItemRes = textItemsRes.get(wordFiltered)
                if (textItemRes == null) {
                    textItemRes = new HashSet()
                    textItemsRes.put(wordFiltered, textItemRes)
                }

                // В случае, если из одного слова в itemText получилось несколько слов в wordsFiltered -
                // также разделим исходный общий большой прямоугольник положения текста
                // на несколько меньших, для каждого нового wordFiltered, пропорционально длине нового слова.
                Set<Map> textItemPositionsSplitted = splitTextItemSinglePosition(textItemPositions, wordsFiltered, wordFiltered)
                textItemRes.addAll(textItemPositionsSplitted)
            }
        }

        return textItemsRes
    }


    /**
     * Пытается угадать формы слова
     *
     * @param wordEng слово
     * @return дополнительные формы слова
     */
    private Collection<String> transformWord(String wordEng) {
        // boy[s] -> boy
        // witness[es] -> witness
        if (wordEng.endsWith("s") && wordEng.length() >= 3) {
            String wordEng_1 = wordEng.substring(0, wordEng.length() - 1)

            // witnesses -> witness
            if (wordEng.endsWith("es") && wordEng.length() >= 4) {
                String wordEng_2 = wordEng.substring(0, wordEng.length() - 2)
                return [wordEng_1, wordEng_2]
            }

            return [wordEng_1]
        }


        // min[ed] -> mine
        if (wordEng.endsWith("ed") && wordEng.length() >= 4) {
            wordEng = wordEng.substring(0, wordEng.length() - 2)
            return [wordEng]
        }

        // dream[ing] -> dream
        if (wordEng.endsWith("ing") && wordEng.length() >= 5) {
            wordEng = wordEng.substring(0, wordEng.length() - 3)
            return [wordEng]
        }

        //
        return null
    }


    private Set<Map> splitTextItemSinglePosition(Set<Map> textItemPositions, List<String> words, String thisWord) {
        Set<Map> res = new HashSet<>()

        //
        int wordsCarsCount = 0
        for (String word : words) {
            wordsCarsCount = wordsCarsCount + word.size()
        }
        //
        int wordCharPos = 0
        for (String word : words) {
            if (thisWord.equals(word)) {
                break
            }
            wordCharPos = wordCharPos + word.size()
        }
        //
        int wordCarsCount = thisWord.size()

        //
        for (Map textItemSinglePosition : textItemPositions) {
            int topPx = textItemSinglePosition.get("top")
            int leftPx = textItemSinglePosition.get("left")
            int heightPx = textItemSinglePosition.get("height")
            int widthPx = textItemSinglePosition.get("width")
            //
            double pxByChar = widthPx / wordsCarsCount
            leftPx = leftPx + (pxByChar * wordCharPos)
            widthPx = (pxByChar * wordCarsCount)
            //
            Map textItemSinglePositionNew = new HashMap(textItemSinglePosition)
            textItemSinglePositionNew.put("top", topPx)
            textItemSinglePositionNew.put("left", leftPx)
            textItemSinglePositionNew.put("height", heightPx)
            textItemSinglePositionNew.put("width", widthPx)
            res.add(textItemSinglePositionNew)
        }

        return res
    }


    /**
     * Формируем пары из продряд идущих слов.
     *
     * @param text подряд идущие слова
     * @return сформированные пары
     */
    private Collection<String> createPairs(Collection<String> text) {
        Collection<String> res = new ArrayList<>()

        for (int i = 0; i < text.size() - 1; i++) {
            res.add(text.get(i) + " " + text.get(i + 1))
        }

        return res
    }


    // endregion


}
