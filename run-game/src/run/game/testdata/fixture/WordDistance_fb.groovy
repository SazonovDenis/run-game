package run.game.testdata.fixture

import jandcode.core.dbm.fixture.*
import jandcode.core.store.*
import run.game.util.*

/**
 *
 */
class WordDistance_fb extends BaseFixtureBuilder {

    String dirBase = "data/web-grab/"

    //
    protected void onBuild() {
        FixtureTable fxWordDistance = fx.table("WordDistance")
        Store stWordDistance = fxWordDistance.getStore()

        // Заполним из наших csv
        RgmCsvUtils utils = mdb.create(RgmCsvUtils)
        utils.addFromCsv(stWordDistance, dirBase + "word_distance.csv")
    }

}