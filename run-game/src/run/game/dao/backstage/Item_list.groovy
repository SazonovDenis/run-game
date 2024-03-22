package run.game.dao.backstage

import jandcode.commons.*
import jandcode.core.dao.*
import jandcode.core.dbm.mdb.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.model.service.*
import run.game.testdata.fixture.*

class Item_list extends BaseMdbUtils {


    int MAX_COUNT_FOUND = 10

    /**
     * Ищем Item по фрагменту написания,
     * среди наших словарных слов
     *
     * @param itemText фрагмент для поиска
     * @return Store структуры Item
     */
    @DaoMethod
    Store find(String text) {
        // Текст с разделителями?
        List lst = text.split("[ \n\r]")

        //
        if (lst.size() > 1) {
            // Ищем много слов целиком
            return findText(lst)
        } else {
            // Ищем одно слово по фрагменту
            return findWord(text)
        }
    }

    Store findWord(String wordText) {
        Store stItem = mdb.createStore("Item.list")

        //
        wordText = wordText.toLowerCase()
        wordText = wordText.trim()

        //
        Fact_list list = mdb.create(Fact_list)

        //
        int count = 0
        Store stFact = list.findFactsByValueDataType(wordText, RgmDbConst.DataType_word_spelling)
        for (StoreRecord rec : stFact) {
            count++
            if (count > MAX_COUNT_FOUND) {
                break
            }

            //stFacts.add(rec.getValues())
            stItem.add([id: rec.getValue("item"), value: rec.getValue("itemValue")])
        }

        //
        count = 0
        stFact = list.findFactsByValueDataType(wordText, RgmDbConst.DataType_word_translate)
        for (StoreRecord rec : stFact) {
            count++
            if (count > MAX_COUNT_FOUND) {
                break
            }

            //stFacts.add(rec.getValues())
            stItem.add([id: rec.getValue("item"), value: rec.getValue("itemValue")])
        }


        //
        return stItem
    }


    /**
     * Ищем Item по точному написанию,
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
     * Ищем Item по точному написанию,
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
     * Ищем Item по точному написанию
     *
     * @param itemsText очищенный список слов
     * @param wordsNotFound для слов, не найденных в словаре
     * @return слова, найденные в словаре
     */
    Store loadBySpelling(Collection<String> itemsText, Collection<String> wordsNotFound) {
        Store stItem = mdb.createStore("Item.list")

        // Получаем из кэша spelling для всех слов
        WordCacheService wordService = mdb.getModel().bean(WordCacheService)
        StoreIndex idxFacts = wordService.getIdxFacts()


        // Отберем среди itemsText те слова, котрые мы знаем (idxFacts)
        Set<String> setItemsText = new HashSet<>()
        for (String itemText : itemsText) {
            // Повторы не нужны
            if (setItemsText.contains(itemText)) {
                continue
            }

            //
            StoreRecord rec = idxFacts.get(itemText)
            if (rec != null) {
                stItem.add([id: rec.getValue("item"), value: rec.getValue("itemValue")])
                setItemsText.add(itemText)
            } else {
                wordsNotFound.add(itemText)
            }
        }


/*
        todo: теги пока не нужны и не используются
        // Загрузим тэги
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
*/


        //
        return stItem
    }

    String sqlItemTag(Set ids) {
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
        word = word.replace("/", "")
        word = word.replace("-", "")
        word = word.replace("_", "")
        word = word.replace("(", "")
        word = word.replace(")", "")
        word = word.replace("!", "")

        //
        if ((UtWord.isAlphasEng(word) || UtWord.isAlphasKaz(word)) && word.length() > 1) {
            return word.toLowerCase()
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
        if (wordEng.endsWith("s")) {
            wordEng = wordEng.substring(0, wordEng.length() - 1)
            return [wordEng]
        }

        if (wordEng.endsWith("ed")) {
            wordEng = wordEng.substring(0, wordEng.length() - 1)
            return [wordEng]
        }

        if (wordEng.endsWith("ing")) {
            wordEng = wordEng.substring(0, wordEng.length() - 3)
            return [wordEng]
        }

        return null
    }

    public static Collection<String> splitWord(String word) {
        Collection<String> words

        words = word.split("[^a-zA-Z0-9']")
        if (words.size() > 1) {
            return words
        }

        return null
    }

}
