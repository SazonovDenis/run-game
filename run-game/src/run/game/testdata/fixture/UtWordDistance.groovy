package run.game.testdata.fixture

import jandcode.commons.*
import jandcode.core.dbm.mdb.*
import jandcode.core.store.*
import org.apache.commons.lang3.*
import org.slf4j.*
import run.game.dao.*
import run.game.util.*

/**
 * Для каждого слова расчитывает ближашие слова.
 * Алгоритм имеет квадратичную сложность, поэтому запускается из тестов единожды,
 * результат его работы  записиваетсмя в файл "WordDistance.csv",
 * и в дальнейшем используется именно этот файл.
 */
class UtWordDistance extends BaseMdbUtils {

    //
    int maxMatchSize = 25

    Logger log = UtLog.getLogConsole()

    LogerFiltered logger = new LogerFiltered(log)

    int id = 1


    public void fillStore(Store stWordDistance) {
        // Получим список слов
        // В запросе distinct написан из-за наличия возможных синонимов среди переводимых слов.
        // Например, в словарях есть два английских слова: "watermelon" и "water melon",
        // которые переводятся одинаково "арбуз", из-за чего
        // Fact типа "перевод на русский" (word-translate) имеется в двух записях,
        // а нам дубликаты слов не нужны
        Store stWordRus = mdb.loadQuery("select distinct value from Fact where dataType = " + RgmDbConst.DataType_word_translate)
        // Рассчитаем расстояние до синонимов
        fillStore_internal(stWordRus, "rus", stWordDistance)

        // Получим список слов
        Store stWordEng = mdb.loadQuery("select distinct value from Fact where dataType = " + RgmDbConst.DataType_word_spelling)
        // Рассчитаем расстояние до синонимов
        fillStore_internal(stWordEng, "eng", stWordDistance)

        //RgmCsvUtils.saveToCsv(stWordRus, new File("temp/stWordRus.csv"))
        //RgmCsvUtils.saveToCsv(stWordEng, new File("temp/stWordEng.csv"))
    }


    public void toCsvFile(File file) {
        Store stWordDistance = mdb.createStore("WordDistance.list")

        //
        fillStore(stWordDistance)

        //
        RgmCsvUtils utils = mdb.create(RgmCsvUtils)
        utils.saveToCsv(stWordDistance, file)
    }


    /**
     * Возвращает лучшие соответствия для слова word по расстонию Джаро-Винклера
     *
     * @param word слово
     * @param stWords другие слова
     * @return лучшие соответсвия
     */
    public static Map<String, Double> getJaroWinklerMatch(String word, Store stWords, int maxMatchSize) {
        Map<String, Double> distances = new HashMap<>()

        //
        double distanceMinLevelToUse = 0.7

        //
        for (int j = 0; j < stWords.size(); j++) {
            String wordPair = stWords.get(j).getString("value")
            // Самого с собой не сравниваем
            if (word.equals(wordPair)) {
                continue
            }
            // Заметно различающиеся по длине
            if (word.length() / wordPair.length() > 2) {
                continue
            }

            //
            double distancePair = StringUtils.getJaroWinklerDistance(word, wordPair)

            //
            if (distancePair > distanceMinLevelToUse) {
                // Вытесняем самую плохую пару
                if (distances.size() >= maxMatchSize) {
                    double distanceMin = 1E9
                    String distanceMinWord = null
                    for (String distanceWord : distances.keySet()) {
                        Double distance = distances.get(distanceWord)
                        if (distance < distanceMin) {
                            distanceMin = distance
                            distanceMinWord = distanceWord
                        }
                    }
                    if (distancePair > distanceMin) {
                        distances.remove(distanceMinWord)
                    }
                }
                // Добавляем новую пару, если место освободилось
                if (distances.size() < maxMatchSize) {
                    distances.put(wordPair, distancePair)
                }
            }
        }


        //
        return distances
    }

    /**
     * Рассчитаем расстояние до синонимов для каждого слова из stWords
     */
    void fillStore_internal(Store stWords, String lang, Store stWordDistance) {
        logger.logStepStart(stWords.size())

        //
        for (StoreRecord rec : stWords) {
            String word = rec.getString("value")

            //
            Map<String, Double> distances = getJaroWinklerMatch(word, stWords, maxMatchSize)

            //
            stWordDistance.add([id: id, word: word, lang: lang, matches: UtJson.toJson(distances)])
            id = id + 1

            //
            logger.logStepStep()
        }
    }


}