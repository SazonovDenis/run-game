package run.game.testdata.fixture

import jandcode.core.dbm.fixture.*
import jandcode.core.store.*
import run.game.util.*

/**
 *
 */
class Usr_fb extends BaseFixtureBuilder {

    protected void onBuild() {
        FixtureTable fxUsr = fx.table("Usr")

        // Заполним из наших csv
        Store stTagCsv = fxUsr.getStore()
        RgmCsvUtils utils = mdb.create(RgmCsvUtils)
        utils.addFromCsv(stTagCsv, "res:run/game/testdata/csv/Usr.csv")
    }

}