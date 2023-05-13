package run.game.testdata.fixture

import jandcode.commons.*
import jandcode.core.dbm.mdb.*
import jandcode.core.store.*
import org.slf4j.*
import run.game.dao.*
import run.game.dao.backstage.*
import run.game.util.*

/**
 *
 */
class WordDistance_list extends BaseMdbUtils {

    //
    int maxMatchSize = 25

    Logger log = UtLog.getLogConsole()

    LogerFiltered logger = new LogerFiltered(log)

    int id = 1

    public void fillStore(Store stWordDistance) {
        // В запросе distinct учитывает наличие возможных синонимов среди переводимых слов,
        // например, есть два английских слова: "watermelon" и "water melon",
        // которые переводятся одинаково "арбуз", из-за чего Fact типа word-translate
        // имеется в двух записях
        Store stWordRus = mdb.loadQuery("select distinct value from Fact where dataType = " + RgmDbConst.DataType_word_translate)
        fill_internal(stWordRus, "rus", stWordDistance)

        //
        Store stWordEng = mdb.loadQuery("select distinct value from Fact where dataType = " + RgmDbConst.DataType_word_spelling)
        fill_internal(stWordEng, "end", stWordDistance)

        //RgmCsvUtils.saveToCsv(stWordRus, new File("temp/stWordRus.csv"))
        //RgmCsvUtils.saveToCsv(stWordEng, new File("temp/stWordEng.csv"))
    }

    public void toCsvFile(File file) {
        Store stWordDistance = mdb.createStore("WordDistance.csv")

        //
        fillStore(stWordDistance)

        //
        RgmCsvUtils utils = mdb.create(RgmCsvUtils)
        utils.saveToCsv(stWordDistance, file)
    }

    void fill_internal(Store stWords, String lang, Store stWordDistance) {
        logger.logStepStart(stWords.size())

        //
        for (StoreRecord rec : stWords) {
            String word = rec.getString("value")

            //
            Map<String, Double> distances = UtWordDistance.getJaroWinklerMatch(word, stWords, maxMatchSize)

            //
            stWordDistance.add([id: id, word: word, lang: lang, matches: UtJson.toJson(distances)])
            id = id + 1

            //
            logger.logStepStep()
        }
    }


}