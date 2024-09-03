package run.game.dao.backstage

import groovy.transform.*
import jandcode.core.dbm.mdb.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.dao.backstage.impl.*
import run.game.model.service.*

/**
 * Поиск по словам или тексту.
 */
@TypeChecked
class Item_find extends BaseMdbUtils {


    public int MAX_COUNT_FOUND = 20
    public int MAX_COUNT_FOUND_ONE_TYPE = 10


    /**
     * Выполняет поиск элементов textPositionsRaw в словарях.
     * Элементы пердварительно очищаются и преобразовываются.
     *
     * @param textPositionsRaw Текстовые фрагменты
     * @param tags Фильтр по тэгам, например по языку
     * @param textPositionsRes Найденные позиции. Каждый элемент ключом имеет позицию,
     *                         значением - набор item, которые нашлись по тексту в позиции.
     * @return Найденные слова: store со списком Item-ов и Fact-ов. Каждый элемент ключом имеет item,
     * значением - набор позиций в источнике, где этот item встретился
     */
    Store collectItems(List<TextPosition> textPositionsRaw, Map<Long, String> tags, List<TextPosition> textPositionsRes) {
        List<TextPosition> textPositions


        // --- Обработка списка на поиск: слияние по переносам
        textPositions = splitWordDivisions(textPositionsRaw)


        // --- Обработка списка на поиск: фильтрация и чистка
        textPositions = handleTextPositions(textPositions, true, (textPosition, idx) -> {
            return UtWord.filterAndSplitWord(textPosition.text)
        })


        // --- Обработка списка на поиск: стоп-слова

        // Получаем из кэша стоп-слова
        WordCacheService wordService = mdb.getModel().bean(WordCacheService)
        StoreIndex idxOcrStopWords = wordService.getIdxOcrStopWords()

        // Фильтруем по стоп-словам
        textPositions = handleTextPositions(textPositions, true, (textPosition, idx) -> {
            List res = new ArrayList()
            if (idxOcrStopWords.get(textPosition.text) == null) {
                res.add(textPosition.text)
            }
            return res
        })


        // --- Формируем результирующие списки

        // Store
        Store stItemRes = mdb.createStore("Item.find")

        // List
        if (textPositionsRes != null) {
            // Список позиций, поступивший в первый поиск, безусловно попадет в результат
            textPositionsRes.addAll(textPositions)
        }


        // --- Поиск по точному совпадению

        // Формируем пары из продряд идущих слов.
        // Так можно попытаться найти словарное слово, которое состоит из нескольких слов,
        // например: "go on", "он екі".
        List<TextPosition> textPositionPairs = createPairs(textPositions)

        // Ищем Items по точному совпадению с textPositions
        Store stItemFound = findTextExact(textPositionPairs, tags)


        // --- Обновляем результирующие списки

        // Store
        // Первая порция поиска поступает в исходно пустой store, поэтому копируется полностью
        copyToStore(stItemFound, stItemRes)

        // List
        // Первая порция поиска поступает в список позиций в том случае,
        // если что-то нашлось сверх того, что поступило на поиск.
        // В этой итерации свверх ранее добавленного будут ПАРЫ слов.
        copyToList(stItemFound, textPositionPairs, textPositionsRes)


        // --- Поиск того, что не нашлось: по точному совпадению, но с перобразованем словоформ

        // Проверим, какие слова не нашлись по непосредственному написанию
        StoreIndex idxItem = stItemRes.getIndex("value")
        List<TextPosition> textItemsNotFound = new ArrayList<>()
        //
        for (TextPosition textPosition : textPositions) {
            if (idxItem.get(textPosition.text) == null) {
                textItemsNotFound.add(textPosition)
            }
        }

        // Для той части слов, которые мы не смогли найти,
        // выполняем преобразование словоформ и делаем вторую попытку
        List<TextPosition> textItemsTranformed = handleTextPositions(textItemsNotFound, false, (textPosition, idx) -> {
            return transformWord(textPosition.text)
        })

        if (textItemsTranformed.size() != 0) {
            // Формируем пары из продряд идущих слов.
            // Так можно попытаться найти словарное слово, которое состоит из нескольких слов,
            // например: "go on", "он екі".
            textPositionPairs = createPairs(textItemsTranformed)

            // Ищем Items по преобразованным словам
            Store stItemFoundTransformed = findTextExact(textPositionPairs, tags)


            // --- Обновляем результирующие списки

            // Store
            // Из второй порции поиска stItemFoundTransformed в stItemRes поступают
            // те записи, которых не было после первого поиска
            copyToStore(stItemFoundTransformed, stItemRes)

            // List
            // Вторая порция поиска поступает в список позиций в том случае,
            // если что-то нашлось сверх того, что поступило на поиск.
            // В этой итерации свверх ранее добавленного будут пары ПРЕОБРАЗОВАННЫХ слов.
            copyToList(stItemFoundTransformed, textPositionPairs, textPositionsRes)
        }


        // --- Поиск по фрагменту

        // Если искали одно слово и нашили мало - то поищем ещё и по фрагменту
        if (textPositions.size() == 1 && stItemRes.size() <= MAX_COUNT_FOUND) {
            // Ищем Items по фрагменту
            TextPosition textPositionWord = textPositions.get(0)
            String word = textPositionWord.text
            Store stItemFoundWord = findWordPart(word, tags)


            // --- Обновляем результирующие списки

            // Store
            copyToStore(stItemFoundWord, stItemRes)

            // List
            copyToListOnePosition(stItemFoundWord, textPositionWord, textPositionsRes)
        }


        // ---
        return stItemRes
    }


    void copyToStore(Store stItemFound, Store stItemRes) {
        StoreIndex idxItemRes = stItemRes.getIndex("id")
        for (StoreRecord rec : stItemFound) {
            if (idxItemRes.get(rec.getLong("id")) == null) {
                stItemRes.add(rec)
            }
        }
    }

    /**
     * Копирует из искомых позиций textPositionsSearched в textPositionsRes те элементы,
     * которые есть в результататах поиска stItemFound
     *
     * @param stItemFound Найденные записи
     * @param textPositionsSearched Позиции, которые искали
     * @param textPositionsRes Сюда пополняем найденные
     */
    void copyToList(Store stItemFound, List<TextPosition> textPositionsSearched, List<TextPosition> textPositionsRes) {
        if (textPositionsRes == null) {
            return
        }

        //
        StoreIndex idxItemFound = stItemFound.getIndex("value")
        //
        for (TextPosition textPosition : textPositionsSearched) {
            if (idxItemFound.get(textPosition.text) != null) {
                textPositionsRes.add(textPosition)
            }
        }
    }

    /**
     * Копирует одну искомую textPosition в textPositionsRes столько раз,
     * сколько есть в результататах поиска stItemFound
     */
    void copyToListOnePosition(Store stItemFound, TextPosition textPosition, List<TextPosition> textPositionsRes) {
        if (textPositionsRes == null) {
            return
        }

        //
        for (StoreRecord rec : stItemFound) {
            String text = rec.getString("value")
            textPositionsRes.add(new TextPosition(text, textPosition))
        }
    }


    /**
     * Ищем СПИСОК слов, каждый по ПОЛНОМУ написанию или переводу,
     * среди наших словарных слов.
     * Возвращаем именно item, а не fact со spelling-ом или translate-ом этого item,
     * т.к. мы ищем именно СЛОВА (item) по написанию а не НАПИСАНИЯ слов.
     *
     * @param text список слов (очищенный)
     * @return Найденные слова: store со списком Item
     */
    Store findTextExact(List<TextPosition> textPositions, Map<Long, String> tagsParam) {
        Store stItem = mdb.createStore("Item.find")

        // Уберем повторы в textPositions - превратим их в Set
        Set<String> setWords = new HashSet<>()
        for (TextPosition textItem : textPositions) {
            setWords.add(textItem.text)
        }

        // Получаем из кэша spelling и translate для всех слов
        WordCacheService wordService = mdb.getModel().bean(WordCacheService)
        Map<Object, List<StoreRecord>> idxFacts = wordService.getIdxFacts()

        // Отберем среди setWords те слова, котрые есть в наших словарях
        Set<Long> setItems = new HashSet<>()
        for (String word : setWords) {
            List<StoreRecord> recsFact = idxFacts.get(word)
            if (recsFact != null) {
                for (StoreRecord recFact : recsFact) {
                    long itemId = recFact.getLong("item")

                    // Повторы не нужны
                    if (setItems.contains(itemId)) {
                        continue
                    }

                    // Фильтр по тэгам
                    boolean wasTagComparationTrue = true
                    Map factTags = (Map) recFact.getValue("tag")
                    if (factTags != null && tagsParam != null && tagsParam.size() != 0) {
                        wasTagComparationTrue = false
                        for (Map.Entry<Long, String> entry : tagsParam.entrySet()) {
                            long tagKey = entry.getKey()
                            String tagValue = entry.getValue()
                            if (tagValue.equals(factTags.get(tagKey))) {
                                wasTagComparationTrue = true
                                break
                            }
                        }
                    }
                    if (!wasTagComparationTrue) {
                        continue
                    }

                    // Возвращаем именно item, а не fact со spelling-ом или translate-ом этого item,
                    // т.к. мы ищем СЛОВА (item) по написанию, а не их НАПИСАНИЯ или ПЕРЕВОДЫ (fact)
                    stItem.add([
                            id   : itemId,
                            value: recFact.getValue("itemValue"),
                            tag  : factTags
                    ])
                    setItems.add(itemId)
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
    Store findWordPart(String word, Map<Long, String> tags) {
        Store stItem = mdb.createStore("Item.find")

        //
        int count
        Fact_list list = mdb.create(Fact_list)
        Set<Long> setItemsFound = new HashSet<>()


        // ---
        // Поиск

        Map<Long, String> tagsValue_word_lang = null
        Map<Long, String> tagsValues_word_translate_direction = null
        if (tags != null) {
            tagsValue_word_lang = [(RgmDbConst.TagType_word_lang): tags.get(RgmDbConst.TagType_word_lang)]
            tagsValues_word_translate_direction = [(RgmDbConst.TagType_word_translate_direction): tags.get(RgmDbConst.TagType_word_translate_direction)]
        }

        // Поиск по атрибуту word_spelling
        Store stFactSpelling = list.findBy_value_factType_tags(RgmDbConst.FactType_word_spelling, word, tagsValue_word_lang)
        stFactSpelling.sort("itemValue")


        // Поиск по атрибуту word_translate
        Store stFactTranslate = list.findBy_value_factType_tags(RgmDbConst.FactType_word_translate, word, tagsValues_word_translate_direction)
        stFactTranslate.sort("factValue")


        // ---
        // Перенос найденных слов в результат


        // В первую очередь перенесём слова, НАЧИНАЮЩИЕСЯ на фрагмент.

        // Результат по атрибуту word_spelling.
        count = 0
        for (StoreRecord rec : stFactSpelling) {
            long itemId = rec.getLong("item")
            String value = rec.getString("itemValue")
            Object factTags = rec.getValue("tag")

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
            stItem.add([
                    id   : itemId,
                    value: value,
                    tag  : factTags,
            ])
            setItemsFound.add(itemId)
        }


        // Результат по атрибуту word_translate.
        count = 0
        for (StoreRecord rec : stFactTranslate) {
            long item = rec.getLong("item")
            String factValue = rec.getString("factValue")
            String value = rec.getString("itemValue")
            Object factTags = rec.getValue("tag")

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
            stItem.add([
                    id   : item,
                    value: value,
                    fact : rec.getValue("id"),
                    tag  : factTags,
            ])
            setItemsFound.add(item)
        }


        // Во вторую очередь слова СОДЕРЖАЩИЕ фрагмент.

        // Результат по атрибуту word_spelling.
        count = 0
        for (StoreRecord rec : stFactSpelling) {
            long item = rec.getLong("item")
            String value = rec.getString("itemValue")
            Object factTags = rec.getValue("tag")

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
            stItem.add([
                    id   : item,
                    value: rec.getValue("itemValue"),
                    tag  : factTags,
            ])
            setItemsFound.add(item)
        }


        // Результат по атрибуту word_translate.
        count = 0
        for (StoreRecord rec : stFactTranslate) {
            long item = rec.getLong("item")
            String factValue = rec.getString("factValue")
            String value = rec.getString("itemValue")
            Object factTags = rec.getValue("tag")

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
            stItem.add([
                    id   : item,
                    value: value,
                    fact : rec.getValue("id"),
                    tag  : factTags,
            ])
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
                    mapItemTag.put(recItemTag.getLong("tagType"), recItemTag.getString("tagValue"))
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
    ItemTag.*
from
    ItemTag
where
    ItemTag.item in ( ${strIds} )
"""
    }
*/


    // region Внутренние утилиты


    interface IWordAnalyzer {
        List<String> handleText(TextPosition textPosition, int idx)
    }


    /**
     * Обрабатывает каждый элемент из textPositions с помощью обработчика wordAnalyzer.
     *
     * @param textPositions список вхождений текстов, где ключ - текст, а значения - набор мест расположения текста на изображении
     * @param doSharePosition делить ли позицию, если из одного слова получилось несколько
     * @param wordAnalyzer анализатор, сколько вернет слов - столько и добавим
     *
     * @return Новый список. Может быть больше или меньше исходного списка.
     */
    private List<TextPosition> handleTextPositions(List<TextPosition> textPositions, boolean doSharePosition, IWordAnalyzer wordAnalyzer) {
        List<TextPosition> textPositionsRes = new ArrayList<>()

        for (int idx = 0; idx < textPositions.size(); idx++) {
            TextPosition textPosition = textPositions.get(idx)
            List<String> textHandled = wordAnalyzer.handleText(textPosition, idx)

            if (doSharePosition && textHandled.size() > 1) {
                // В случае, если из одного слова в textPosition получилось несколько слов в textHandled -
                // также разделим исходный общий большой прямоугольник положения текста
                // на несколько меньших, для каждого нового word, пропорционально длине нового слова.
                List<TextPosition> textPositionsNew = splitTextItemSinglePosition(textHandled, textPosition.position)
                textPositionsRes.addAll(textPositionsNew)
            } else {
                //
                for (String word : textHandled) {
                    TextPosition textPositionNew = new TextPosition(word, textPosition)
                    textPositionsRes.addAll(textPositionNew)
                }
            }
        }

        return textPositionsRes
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
        // warm[ed] -> warm
        if (wordEng.endsWith("ed") && wordEng.length() >= 4) {
            // min[ed] -> mine
            String wordEng_1 = wordEng.substring(0, wordEng.length() - 1)

            // warm[ed] -> warm
            String wordEng_2 = wordEng.substring(0, wordEng.length() - 2)
            return [wordEng_1, wordEng_2]
        }

        // dream[ing] -> dream
        if (wordEng.endsWith("ing") && wordEng.length() >= 5) {
            wordEng = wordEng.substring(0, wordEng.length() - 3)
            return [wordEng]
        }

        //
        return null
    }

    private List<TextPosition> splitTextItemSinglePosition(List<String> textHandled, Position positionBase) {
        List<TextPosition> res = new ArrayList<>()

        //
        if (textHandled == null || textHandled.size() == 0) {
            return res
        }

        //
        int textHandledChars = 0
        for (String wordHandled : textHandled) {
            textHandledChars = textHandledChars + wordHandled.size()
        }
        //
        double pxByChar = positionBase.width / textHandledChars


        //
        int textHandledCharsPos = 0
        for (String wordHandled : textHandled) {
            int leftPx = (int) (pxByChar * textHandledCharsPos)
            int widthPx = (int) (pxByChar * wordHandled.length())

            //

            TextPosition textPosition = new TextPosition(
                    wordHandled,
                    new Position(
                            positionBase.left + leftPx,
                            positionBase.top,
                            widthPx,
                            positionBase.height),
                    null,
            )
            //
            res.add(textPosition)

            //
            textHandledCharsPos = textHandledCharsPos + wordHandled.length()
        }

        //
        return res
    }


    /**
     * Формирует пары из продряд идущих слов.
     *
     * @param text подряд идущие слова
     * @return сформированные пары
     */
    private List<TextPosition> createPairs(List<TextPosition> textPositions) {
        List<TextPosition> res = new ArrayList<>()

        // Оригинальный список
        res.addAll(textPositions)

        // Пары
        for (int idx = 0; idx < textPositions.size() - 1; idx++) {
            TextPosition textPosition1 = textPositions.get(idx)
            TextPosition textPosition2 = textPositions.get(idx + 1)
            // Текст берется из двух соседних позиций
            String textPair = textPosition1.text + " " + textPosition2.text
            // Прямоугольник берется от первого слова
            TextPosition textPositionPair = new TextPosition(textPair, textPosition1)
            //
            res.add(textPositionPair)
        }

        //
        return res
    }


    /**
     * Обнаружение переносов и формирование слитых слов
     *
     * @param textPositions
     * @return
     */
    List<TextPosition> splitWordDivisions(List<TextPosition> textPositions) {
        List<TextPosition> res = new ArrayList<>()

        // --- Обработка списка на поиск: слияние по переносам
        for (int idx = 0; idx < textPositions.size(); idx++) {
            TextPosition textPosition1 = textPositions.get(idx)
            res.add(textPosition1)

            //
            if (idx > 0) {
                TextPosition textPosition0 = textPositions.get(idx - 1)

                //
                if (textPosition0.text.endsWith("-") && (textPosition1.pagePosition.lineNum - textPosition0.pagePosition.lineNum) == 1) {
                    // Текст сливается из двух слов
                    String textMerged = textPosition0.text.substring(0, textPosition0.text.length() - 1) + textPosition1.text
                    // Прямоугольник берется от первого слова
                    TextPosition textPositionMerged = new TextPosition(
                            textMerged, textPosition0
                    )
                    //
                    res.add(textPositionMerged)
                }
            }
        }

        //
        return res
    }


    // endregion


}
