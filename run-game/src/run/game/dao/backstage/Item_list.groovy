package run.game.dao.backstage

import jandcode.commons.*
import jandcode.core.dao.*
import jandcode.core.dbm.mdb.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.model.service.WordCacheService
import run.game.testdata.fixture.*

class Item_list extends BaseMdbUtils {

    /**
     * Ищем Item по фрагменту написания,
     * среди наших словарных слов
     *
     * @param itemText фрагмент для поиска
     * @return Store структуры Item
     */
    @DaoMethod
    Store find(String itemText) {
        Store stFacts = mdb.createStore("Fact.list")

        if (itemText == null || itemText.length() <= 1) {
            return stFacts
        }
        itemText = itemText.toLowerCase()

        //
        Fact_list list = mdb.create(Fact_list)

        int MAX_COUNT = 10

        //
        int count = 0
        Store stFact = list.findFactsByValueDataType(itemText, RgmDbConst.DataType_word_spelling)
        for (StoreRecord rec : stFact) {
            count++
            if (count > MAX_COUNT) {
                break
            }

            stFacts.add(rec.getValues())
        }

        //
        count = 0
        stFact = list.findFactsByValueDataType(itemText, RgmDbConst.DataType_word_translate)
        for (StoreRecord rec : stFact) {
            count++
            if (count > MAX_COUNT) {
                break
            }

            stFacts.add(rec.getValues())
        }


        //
        return stFacts
    }


    /**
     * Ищем Item по точному написанию,
     * среди наших словарных слов
     *
     * @param itemsText список слов
     * @return Store структуры Item
     */
    Store loadBySpelling(Collection<String> itemsText) {
        // Очищаем слова
        Collection<String> itemsTextFiltered = filterAndTransformText(itemsText)

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
    Store loadBySpelling(String fileName) {
        // Грузим слова из файла
        Collection<String> itemsText = readFromFile(fileName)

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
        Store stItem = mdb.createStore("Item")

        // Получаем spelling для всех слов
        WordCacheService wordService = mdb.getModel().bean(WordCacheService)
        StoreIndex idxFacts = wordService.getIdxFacts()

        //
        Set<String> setItemsText = new HashSet<>()
        for (String itemText : itemsText) {
            // Повторы не нужны
            if (setItemsText.contains(itemsText)) {
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

        //
        return stItem
    }

    /**
     * Читаем файл, очищаем от мусора
     *
     * @param fileName файл со списком слов
     * @return очищенный список слов
     */
    Collection<String> readFromFile(String fileName) {
        // Грузим слова из файла
        String strFile = UtFile.loadString(fileName)
        Collection<String> itemsText = strFile.split()

        // Очищаем слова
        Collection<String> itemsTextFiltered = filterAndTransformText(itemsText)

        //
        return itemsTextFiltered
    }

    /**
     * Очищаем слова от мусора (знаки препинания, дефисы и т.п.)
     *
     * @param words список слов
     * @return очищенный список слов
     */
    Collection<String> filterAndTransformText(Collection<String> words) {
        Collection<String> res = new ArrayList<>()

        for (String word : words) {
            Collection<String> words1 = splitWord(word)
            if (words1 != null) {
                res.addAll(filterAndTransformText(words1))
                continue
            }

            //
            word = filterWord(word)
            if (word == null) {
                continue
            }

            //
            res.add(word)

            //
            words1 = tranformWord(word)
            if (words1 != null) {
                res.addAll(filterAndTransformText(words1))
            }
        }

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


    private String filterWord(String word) {
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
    private Collection<String> tranformWord(String wordEng) {
        if (wordEng.endsWith("s")) {
            wordEng = wordEng.substring(0, wordEng.length() - 1)
            return [wordEng]
        }

        if (wordEng.endsWith("ed")) {
            wordEng = wordEng.substring(0, wordEng.length() - 1)
            return [wordEng]
        }

        return null
    }

    private Collection<String> splitWord(String word) {
        Collection<String> words

        words = word.split("[^a-zA-Z0-9']")
        if (words.size() > 1) {
            return words
        }

        return null
    }

}
