package run.game.testdata.fixture

import jandcode.commons.*
import jandcode.core.dbm.mdb.*
import jandcode.core.store.*
import org.apache.commons.lang3.*
import org.slf4j.*
import run.game.dao.*
import run.game.testdata.*
import run.game.util.*

/**
 * Для каждого слова расчитывает ближашие слова.
 * Алгоритм имеет квадратичную сложность, поэтому запускается из тестов единожды,
 * результат его работы  записываетсмя в файл "WordDistance.csv",
 * и в дальнейшем используется именно этот файл.
 */
class UtWordDistance extends BaseMdbUtils {

    //
    static double DISTANCE_MIN_LEVEL = 0.7
    static int MACH_COUNT_FOR_WORD = 25

    //
    Logger log = UtLog.getLogConsole()
    LogerFiltered logger = new LogerFiltered(log)


    //
    public void fillStore(Store stWordDistance, Store stWordDistanceErrors) {
        logger.logStepSize = 10

        RgmCsvUtils utils = mdb.create(RgmCsvUtils)

        // Получим список слов.
        // В запросе distinct написан из-за наличия возможных синонимов среди переводимых слов.
        // Например, в словарях есть два английских слова: "watermelon" и "water melon",
        // которые переводятся одинаково "арбуз", из-за чего Fact типа "перевод на русский"
        // (word-translate) имеется в двух записях, а для построения синонимов дубликаты не нужны.

        // Получим список слов - rus
        Store stWordRus = mdb.loadQuery(sqlFactsByTag([RgmDbConst.Tag_word_translate_direction_eng_rus, RgmDbConst.Tag_word_translate_direction_kaz_rus]))
        //
        utils.saveToCsv(stWordRus, new File("temp/stWordRus.csv"))

        // Рассчитаем расстояние до синонимов
        fillStore_internal(stWordRus, "rus", stWordDistance, stWordDistanceErrors)


        // Получим список слов - kaz
        Store stWordKaz = mdb.loadQuery(sqlItemsByTag([RgmDbConst.Tag_word_lang_kaz]))
        //
        utils.saveToCsv(stWordKaz, new File("temp/stWordKaz.csv"))

        // Рассчитаем расстояние до синонимов
        fillStore_internal(stWordKaz, "kaz", stWordDistance, stWordDistanceErrors)


        // Получим список слов - eng
        Store stWordEng = mdb.loadQuery(sqlItemsByTag([RgmDbConst.Tag_word_lang_eng]))
        //
        utils.saveToCsv(stWordEng, new File("temp/stWordEng.csv"))

        // Рассчитаем расстояние до синонимов
        fillStore_internal(stWordEng, "eng", stWordDistance, stWordDistanceErrors)


        //
        utils.saveToCsv(stWordDistanceErrors, new File("temp/stWordDistanceErrors.csv"))
    }


    public void toCsvFile(File file) {
        Store stWordDistance = mdb.createStore("WordDistance.list")
        Store stWordDistanceErrors = mdb.createStore("WordDistance.errors")

        //
        fillStore(stWordDistance, stWordDistanceErrors)

        //
        TestdataUtils.numerateId(stWordDistance)

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
            if (distancePair > DISTANCE_MIN_LEVEL) {
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
    void fillStore_internal(Store stWords, String lang, Store stWordDistance, Store stWordDistanceErrors) {
        logger.logStepStart(stWords.size(), lang)

        //
        int id = 1

        //
        for (StoreRecord rec : stWords) {
            String word = rec.getString("value")

            //
            Map<String, Double> distances = getJaroWinklerMatch(word, stWords, MACH_COUNT_FOR_WORD)

            //
            if (distances.size() < MACH_COUNT_FOR_WORD) {
                String error = "distances.size < " + MACH_COUNT_FOR_WORD + ", distances.size: " + distances.size()
                stWordDistanceErrors.add([word: word, lang: lang, error: error])
            }
            if (distances.size() > 0) {
                stWordDistance.add([id: id, word: word, lang: lang, matches: UtJson.toJson(distances)])
                id = id + 1
            }

            //
            logger.logStepStep()
        }
    }


    String sqlItemsByTag(Collection tags) {
        return """
select
    distinct
    Item.value

from
    Item
    join ItemTag on (ItemTag.item = Item.id)

where
    ItemTag.tag in (${tags.join(",")})

order by
    Item.value
"""
    }


    String sqlFactsByTag(Collection tags) {
        return """
select
    distinct
    Fact.value

from
    Fact
    join FactTag on (FactTag.fact = Fact.id)

where
    FactTag.tag in (${tags.join(",")})

order by
    Fact.value
"""
    }


}