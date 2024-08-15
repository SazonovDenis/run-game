package run.game.dao.backstage

/**
 * Очистка и разбивка слов из загрязненного текста.
 */
class UtWord {


    public static boolean isAlphasEng(String word) {
        return word != null && word.matches("^[a-zA-Z )(/’!-]*\$")
    }

    public static boolean isAlphasRus(String word) {
        return word != null && word.matches("^[а-яА-Я .,)(/ё-]*[?!]?\$")
    }

    public static boolean isAlphasKaz(String word) {
        return word != null && word.matches("^[а-яА-Я .,ӘҒҚҢӨҰҮҺІIәғқңөұүһіi)(/ё-]*[?!]?\$")
    }

    public static isValidLangSymbols(String word) {
        return isAlphasEng(word) || isAlphasRus(word) || isAlphasKaz(word)
    }

    /**
     * Обрабатываем список слов, см. {@link #filterAndSplitWord}.
     *
     * @param text список слов
     * @return очищенный список слов
     */
    public static Collection<String> filterAndSplitText(Collection<String> text) {
        Collection<String> res = new ArrayList<>()

        for (String word : text) {
            Collection<String> wordsFiltered = filterAndSplitWord(word)
            if (wordsFiltered != null) {
                res.addAll(wordsFiltered)
            }
        }

        return res
    }

    /**
     * Обрабатываем слово, получаем одно или несколько слов, пригодных для поиска в словарях.
     * Очищаем от мусора (неалфавитные символы),
     * разделяем по разделителям (знаки препинания, дефисы и т.п.).
     *
     * @param word слово
     * @return очищенный список слов
     */
    public static Collection<String> filterAndSplitWord(String word) {
        Collection<String> res = new ArrayList<>()

        // Сначала попробоем разделить на слова по разделителям
        Collection<String> wordsSplitted = splitWord(word)
        if (wordsSplitted != null) {
            res.addAll(filterAndSplitText(wordsSplitted))
            return res
        }

        // Не удалось разделить на слова - значит это одиночное слово, убираем из него мусор
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
     * Разделяет слово по разделителям
     *
     * @param word слово
     * @return список разделенных слов
     */
    private static Collection<String> splitWord(String word) {
        // Вместо регулярки "разделитель - любой, кроме явно указанные символы" (регулярка начинается с домика: [^...)
        // лучше использовать регулярку "разделитель - явно указанные символы"  (регулярка без домика: [...).
        // Это так, потому что тогда не придется явно перечислять все символы всех поддерживаемых языков.
        // Иноми словами, вместо: words = word.split("[^a-zA-Z0-9']")
        // пишем: words = word.split("[ -/\\,.]")
        Collection<String> words = word.split("[ -/\\\\,.:;\n\r)(!?\\[\\]\t]")
        if (words.size() > 1) {
            return words
        }

        //
        return null
    }


    /**
     * Убираем из слова лишние (неалфавитные) символы
     *
     * @param word слово
     * @return очищенное слово
     */
    private static String filterWord(String word) {
        //
        if (word == null)
            return null

        //
        word = word.toLowerCase().trim()
        word = word.replace(",", "")
        word = word.replace(".", "")
        word = word.replace(";", "")
        word = word.replace(":", "")
        word = word.replace("/", "")
        word = word.replace("-", "")
        word = word.replace("+", "")
        word = word.replace("_", "")
        word = word.replace("(", "")
        word = word.replace(")", "")
        word = word.replace("[", "")
        word = word.replace("]", "")
        word = word.replace("{", "")
        word = word.replace("}", "")
        word = word.replace("!", "")
        word = word.replace("?", "")
        word = word.replace("`", "")
        word = word.replace("'", "")

        //
        if (word.length() == 0)
            return null

        // Слово теперь содержит только допустимые символы?
        if (isValidLangSymbols(word)) {
            return word
        }

        //
        return null
    }


}
