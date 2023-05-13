package run.game.testdata.fixture

import jandcode.commons.*
import jandcode.core.dbm.fixture.*
import jandcode.core.store.*
import org.slf4j.*
import run.game.dao.*
import run.game.dao.backstage.*
import run.game.util.*

/**
 *
 */
class WordDistance_fb extends BaseFixtureBuilder {

    //
    int maxMatchSize = 20

    Logger log = UtLog.getLogConsole()

    LogerFiltered logger = new LogerFiltered(log)

    int id = 1

    //
    protected void onBuild() {
        FixtureTable fxWordDistance = fx.table("WordDistance")
        Store stWordSynonym = fxWordDistance.getStore()

        //
        Store stWordRus = mdb.loadQuery("select * from Fact where dataType = " + RgmDbConst.DataType_word_translate)
        doBuild(stWordRus, "rus", stWordSynonym)

        //
        Store stWordEng = mdb.loadQuery("select * from Fact where dataType = " + RgmDbConst.DataType_word_spelling)
        doBuild(stWordEng, "eng", stWordSynonym)
    }

    void doBuild(Store stWords, String lang, Store stWordSynonym) {
        logger.logStepStart(stWords.size())

        //
        for (StoreRecord rec : stWords) {
            String word = rec.getString("value")

            //
            Map<String, Double> distances = UtWordDistance.getJaroWinklerMatch(word, stWords, maxMatchSize)

            //
            stWordSynonym.add([id: id, word: word, lang: lang, matches: UtJson.toJson(distances)])
            id = id + 1

            //
            logger.logStepStep()
        }
    }

}