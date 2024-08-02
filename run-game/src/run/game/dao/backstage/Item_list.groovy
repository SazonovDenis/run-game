package run.game.dao.backstage

import jandcode.commons.*
import jandcode.commons.error.*
import jandcode.core.dao.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.dao.ocr.*
import run.game.model.service.*
import run.game.testdata.fixture.*
import run.game.util.*

class Item_list extends RgmMdbUtils {


    public int MAX_COUNT_FOUND = 20
    public int MAX_COUNT_FOUND_ITEM = 10


    @DaoMethod
    Store findItems(String text, long idPlan) {
        // --- Слова
        Map<String, Set> textItems = new LinkedHashMap<>()
        textItems.put(text, null)

        // --- Поиск слов среди введенного
        Store stItem = collectItems(textItems, null)

        // --- Заполнение свойств найденных слов
        Store stFact = makeFactList(stItem, idPlan)

        // ---
        return stFact
    }


    @DaoMethod
    DataBox findStill(String imgBase64, long idPlan) {
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
        Store stItem = collectItems(textItems, textItemsFiltered)


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


    protected Store collectItems(Map<String, Set> textItemsRaw, Map<String, Set> textItemsFiltered) {
        // --- Обработка найденного: фильтрация и чистка
        Map<String, Set> textItems = filterTextItems(textItemsRaw, (word, item) -> {
            return filterAndSplitWord(word)
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

        // Ищем Items по точному совпадению с textItems
        List<String> wordList = textItems.keySet().toList()
        Store stItem = findText(wordList)


        // --- Поиск с перобразованем словоформ

        // Проверим, какие слова не нашлись по непосредственному написанию
        StoreIndex idxItems = stItem.getIndex("value")
        Map<String, Set> textItemsNotFound = new LinkedHashMap<>()
        //
        for (String word : wordList) {
            if (idxItems.get(word) == null) {
                textItemsNotFound.put(word, textItems.get(word))
            }
        }

        // Если часть слов не нашлось - выполняем преобразование словоформ и делаем вторую попытку
        Map<String, Set> textItemsTranformed = filterTextItems(textItemsNotFound, (word, item) -> {
            return transformWord(word)
        })

        if (textItemsTranformed.size() != 0) {
            List textListTranformed = textItemsTranformed.keySet().toList()

            // Ищем Items по преобразованным text
            Store stItemTransformed = findText(textListTranformed)

            // Обновляем наши списки
            stItemTransformed.copyTo(stItem)
            //
            textItems.putAll(textItemsTranformed)
        }


        // --- Поиск по фрагменту

        // Если искали одно слово и нашили мало - то поищем ещё и по фрагменту
        if (wordList.size() == 1 && stItem.size() <= MAX_COUNT_FOUND) {
            // Ищем Items по фрагменту
            Set itemsFound = stItem.getUniqueValues("value")
            String word = wordList.get(0)
            Store stItemWord = findWord(word)

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
        //
        return stItem
    }

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
    Map<String, Set> filterTextItems(Map<String, Set<Map>> textItems, IWordAnalyzer wordAnalyzer) {
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

    protected Set<Map> splitTextItemSinglePosition(Set<Map> textItemPositions, List<String> words, String thisWord) {
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
     *
     * @param stText данные от Tesseract
     * @param textItems разобранные слова, ключ: слово, значение: список позиций, где встретился
     * @param textItemsPosition отдельно спискок позиций
     */
    protected void splitOcrResult(Store stText, Map<String, Set> textItems, List textItemsPosition) {
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

    protected Store makeOrdered(Store stItem, Store stFact) {
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

    protected Store makeFactList(Store stItem, long idPlan) {
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

    protected Store loadFactList(Store stItem, long idPlan) {
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


    /**
     * Ищем Item или Fact по фрагменту написания,
     * среди наших словарных слов
     *
     * @param itemText фрагмент для поиска
     * @return Store структуры "Item.find"
     */
    Store find(String text) {
        text = text.toLowerCase().trim()

        if (text.length() == 0) {
            throw new XError("Строка для поиска не указана")
        }

        // Текст с разделителями?
        Collection<String> lst = filterAndSplitWord(text)

        //
        if (lst.size() > 1) {
            // Ищем сразу много слов по полному написанию
            return findText(lst)
        } else {
            // Ищем одно слово по слову или фрагменту
            return findWord(text)
        }
    }


    /**
     * Ищем Item по фрагменту написания или перевода,
     * среди наших словарных слов
     *
     * @param itemsText слово или фрагмент
     * @return Store со списком Item
     */
    Store findWord(String wordText) {
        Store stItem = mdb.createStore("Item.find")

        //
        int count = 0

        //
        wordText = wordText.toLowerCase().trim()

        //
        Fact_list list = mdb.create(Fact_list)

        //
        Set<Long> setItems = new HashSet<>()


        // ---
        // Поиск по атрибуту word_spelling (точное соответствие)
        Store stFactExact = list.loadFactsByValueDataType(wordText, RgmDbConst.DataType_word_spelling)

        // Перенесём в результат по атрибуту word_spelling(точное соответствие),
        // без ограничений
        for (StoreRecord rec : stFactExact) {
            long item = rec.getLong("item")
            String value = rec.getString("itemValue")

            // Повторы не нужны
            if (setItems.contains(item)) {
                continue
            }

            //
            stItem.add([id: item, value: value])
            setItems.add(item)
        }


        // ---
        // Поиск по атрибуту word_spelling (по фрагменту)
        Store stFactSpelling = list.findFactsByValueDataType(wordText, RgmDbConst.DataType_word_spelling)
        stFactSpelling.sort("itemValue")


        // Перенесём в результат по атрибуту word_spelling (по фрагменту).
        // В первую очередь слова, НАЧИНАЮЩИЕСЯ на фрагмент.
        count = 0
        for (StoreRecord rec : stFactSpelling) {
            long item = rec.getLong("item")
            String value = rec.getString("itemValue")

            // Сейчас - только слова НАЧИНАЮЩИЕСЯ на фрагмент
            if (!value.startsWith(wordText)) {
                continue
            }

            // Повторы не нужны
            if (setItems.contains(item)) {
                continue
            }

            // Не более MAX_COUNT_FOUND_ITEM в результатах
            if (count > MAX_COUNT_FOUND_ITEM) {
                break
            }
            count++

            //
            stItem.add([id: item, value: value])
            setItems.add(item)
        }


        // Поиск по атрибуту word_translate
        Store stFactTranslate = list.findFactsByValueDataType(wordText, RgmDbConst.DataType_word_translate)
        stFactTranslate.sort("factValue")


        // Перенесём в результат по атрибуту word_translate.
        // В первую очередь слова, НАЧИНАЮЩИЕСЯ на фрагмент.
        count = 0
        for (StoreRecord rec : stFactTranslate) {
            long item = rec.getLong("item")
            String factValue = rec.getString("factValue")
            String value = rec.getString("itemValue")

            // Сейчас - только слова НАЧИНАЮЩИЕСЯ на фрагмент
            if (!factValue.startsWith(wordText)) {
                continue
            }

            // Повторы не нужны
            if (setItems.contains(item)) {
                continue
            }

            // Не более MAX_COUNT_FOUND_ITEM в результатах
            if (count > MAX_COUNT_FOUND_ITEM) {
                break
            }
            count++

            //
            stItem.add([id: item, value: value, fact: rec.getValue("id")])
            setItems.add(item)
        }


        // Перенесём в результат по атрибуту word_spelling (по фрагменту).
        // Во вторую очередь слова СОДЕРЖАЩИЕ фрагмент.
        count = 0
        for (StoreRecord rec : stFactSpelling) {
            long item = rec.getLong("item")
            String value = rec.getString("itemValue")

            // Сейчас - только слова СОДЕРЖАЩИЕ фрагмент
            if (value.startsWith(wordText)) {
                continue
            }

            // Повторы не нужны
            if (setItems.contains(item)) {
                continue
            }

            // Не более MAX_COUNT_FOUND_ITEM в результатах
            if (count > MAX_COUNT_FOUND_ITEM) {
                break
            }
            count++

            //
            stItem.add([id: item, value: rec.getValue("itemValue")])
            setItems.add(item)
        }


        // Перенесём в результат по атрибуту word_translate.
        // Во вторую очередь слова СОДЕРЖАЩИЕ фрагмент.
        count = 0
        for (StoreRecord rec : stFactTranslate) {
            long item = rec.getLong("item")
            String factValue = rec.getString("factValue")
            String value = rec.getString("itemValue")

            // Сейчас - только слова СОДЕРЖАЩИЕ фрагмент
            if (factValue.startsWith(wordText)) {
                continue
            }

            // Повторы не нужны
            if (setItems.contains(item)) {
                continue
            }

            // Не более MAX_COUNT_FOUND_ITEM в результатах
            if (count > MAX_COUNT_FOUND_ITEM) {
                break
            }
            count++

            //
            stItem.add([id: item, value: value, fact: rec.getValue("id")])
            setItems.add(item)
        }


        //
        return stItem
    }


    /**
     * Ищем Item по полному написанию или переводу,
     * среди наших словарных слов
     *
     * @param itemsText список слов
     * @return Store со списком Item
     */
    Store findText(Collection<String> itemsText) {
        // Очищаем слова
        Collection<String> itemsTextFiltered = filterAndSplitText(itemsText)

        // Формируем пары из продряд идущих слов.
        Collection<String> itemsTextPairs = createPairs(itemsTextFiltered)
        itemsTextFiltered.addAll(itemsTextPairs)

        // Ищем слова
        Collection<String> itemsNotFound = new ArrayList<>()
        return loadBySpelling(itemsTextFiltered, itemsNotFound)
    }


    /**
     * Ищем Item по полному написанию или переводу,
     * среди наших словарных слов
     *
     * @param fileName файл со списком слов
     * @return Store структуры Item
     */
    Store findTextFromFile(String fileName) {
        // Грузим слова из файла
        Collection<String> itemsText = readTextFromFile(fileName)

        // Формируем пары из продряд идущих слов.
        Collection<String> itemsTextPairs = createPairs(itemsText)
        itemsText.addAll(itemsTextPairs)

        // Ищем слова
        Collection<String> itemsNotFound = new ArrayList<>()
        return loadBySpelling(itemsText, itemsNotFound)
    }


    /**
     * Ищем Item по полному написанию или переводу
     *
     * @param itemsText очищенный список слов
     * @param wordsNotFound для слов, не найденных в словаре
     * @return слова, найденные в словаре
     */
    Store loadBySpelling(Collection<String> itemsText, Collection<String> wordsNotFound) {
        Store stItem = mdb.createStore("Item.find")

        // Получаем из кэша spelling и translate для всех слов
        WordCacheService wordService = mdb.getModel().bean(WordCacheService)
        Map<Object, List<StoreRecord>> idxFacts = wordService.getIdxFacts()

        // Отберем среди itemsText те слова, котрые есть в наших словарях
        Set<Long> setItems = new HashSet<>()
        for (String itemText : itemsText) {
            List<StoreRecord> recs = idxFacts.get(itemText)
            if (recs != null) {
                for (StoreRecord rec : recs) {
                    long item = rec.getLong("item")

                    // Повторы не нужны
                    if (setItems.contains(item)) {
                        continue
                    }

                    //
                    stItem.add([id: item, value: rec.getValue("itemValue")])
                    setItems.add(item)
                }
            } else {
                wordsNotFound.add(itemText)
            }
        }

        //
        return stItem
    }


    /**
     * Читаем файл, очищаем от мусора
     *
     * @param fileName файл со списком слов
     * @return очищенный список слов
     */
    protected Collection<String> readTextFromFile(String fileName) {
        // Грузим слова из файла
        String strFile = UtFile.loadString(fileName)
        Collection<String> itemsText = strFile.split()

        // Очищаем слова
        Collection<String> itemsTextFiltered = filterAndSplitText(itemsText)

        //
        return itemsTextFiltered
    }

    /**
     * Очищаем слова от мусора (знаки препинания, дефисы и т.п.)
     *
     * @param words список слов
     * @return очищенный список слов
     */
    protected static Collection<String> filterAndSplitText(Collection<String> words) {
        Collection<String> res = new ArrayList<>()

        for (String word : words) {
            Collection<String> words1 = filterAndSplitWord(word)
            if (words1 != null) {
                res.addAll(words1)
            }
        }

        return res
    }

    protected static Collection<String> filterAndSplitWord(String word) {
        Collection<String> res = new ArrayList<>()

        Collection<String> words1 = splitWord(word)
        if (words1 != null) {
            res.addAll(filterAndSplitText(words1))
            return res
        }

        //
        word = filterWord(word)
        if (word == null) {
            return res
        }

        //
        res.add(word)

        //
        return res
    }

    /**
     * Формируем пары из продряд идущих слов.
     * Так можно попытаться найти Item, который состоит из нескольких слов,
     * напимер: "go on", "он екі".
     *
     * @param words
     * @return сформированные пары
     */
    protected Collection<String> createPairs(Collection<String> words) {
        Collection<String> res = new ArrayList<>()

        for (int i = 0; i < words.size() - 1; i++) {
            res.add(words.get(i) + " " + words.get(i + 1))
        }

        return res
    }

    protected static String filterWord(String word) {
        //
        if (word == null)
            return null
        if (word.length() == 0)
            return null

        //
        word = word.replace(",", "")
        word = word.replace(".", "")
        word = word.replace(";", "")
        word = word.replace(":", "")
        word = word.replace("/", "")
        word = word.replace("-", "")
        word = word.replace("_", "")
        word = word.replace("(", "")
        word = word.replace(")", "")
        word = word.replace("!", "")

        //
        if ((UtWord.isAlphasEng(word) || UtWord.isAlphasKaz(word)) && word.length() > 1) {
            return word.toLowerCase().trim()
        }

        //
        return null
    }

    /**
     * Пытается угадать формы слова
     * @param wordEng
     * @return дополнительные формы слова
     */
    protected Collection<String> transformWord(String wordEng) {
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

    protected static Collection<String> splitWord(String word) {
        Collection<String> words

        words = word.split("[^a-zA-Z0-9']")
        if (words.size() > 1) {
            return words
        }

        return null
    }


    private String sqlPlanFactStatistic_Items(Collection items) {
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

    private String sqlPlanFactStatistic_Facts(Collection facts) {
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


}
