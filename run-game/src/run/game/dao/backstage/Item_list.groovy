package run.game.dao.backstage

import jandcode.commons.*
import jandcode.commons.error.*
import jandcode.core.dao.*
import jandcode.core.dbm.mdb.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.model.service.*
import run.game.testdata.fixture.*

class Item_list extends BaseMdbUtils {


    public int MAX_COUNT_FOUND = 20
    public int MAX_COUNT_FOUND_ITEM = 10

    /**
     * Ищем Item или Fact по фрагменту написания,
     * среди наших словарных слов
     *
     * @param itemText фрагмент для поиска
     * @return Store структуры "Item.find"
     */
    @DaoMethod
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
    Collection<String> readTextFromFile(String fileName) {
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
    public static Collection<String> filterAndSplitText(Collection<String> words) {
        Collection<String> res = new ArrayList<>()

        for (String word : words) {
            Collection<String> words1 = filterAndSplitWord(word)
            if (words1 != null) {
                res.addAll(words1)
            }
        }

        return res
    }


    public static Collection<String> filterAndSplitWord(String word) {
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
    Collection<String> createPairs(Collection<String> words) {
        Collection<String> res = new ArrayList<>()

        for (int i = 0; i < words.size() - 1; i++) {
            res.add(words.get(i) + " " + words.get(i + 1))
        }

        return res
    }


    public static String filterWord(String word) {
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
    public static Collection<String> transformWord(String wordEng) {
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

    public static Collection<String> splitWord(String word) {
        Collection<String> words

        //words = word.split("[^a-zA-Z0-9']")
        words = word.split("[ -/\\\\,.]")
        if (words.size() > 1) {
            return words
        }

        return null
    }

}
