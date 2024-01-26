package run.game.testdata.fixture

class UtWord {


    public static boolean isAlphasEng(String s) {
        return s != null && s.matches("^[a-zA-Z )(/’!-]*\$")
    }

    public static boolean isAlphasRus(String s) {
        return s != null && s.matches("^[а-яА-Я .,)(/ё-]*[?!]?\$")
    }

    public static boolean isAlphasKaz(String s) {
        return s != null && s.matches("^[а-яА-Я .,ӘҒҚҢӨҰҮҺІIәғқңөұүһіi)(/ё-]*[?!]?\$")
    }


}
