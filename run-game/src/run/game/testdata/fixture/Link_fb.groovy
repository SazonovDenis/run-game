package run.game.testdata.fixture

import jandcode.core.dbm.fixture.*
import jandcode.core.store.*
import run.game.util.*

/**
 *
 */
class Link_fb extends BaseFixtureBuilder {

    protected void onBuild() {
        FixtureTable fxTable = fx.table("Link")
        fxTable.rangeId(2000)

        // Заполним из наших csv
        Store st = mdb.createStore("Link")
        RgmCsvUtils utils = mdb.create(RgmCsvUtils)
        utils.addFromCsv(st, "res:run/game/testdata/csv/Link.csv")

        //
        for (StoreRecord rec : st) {
            fxTable.add(rec.values)
        }
    }

}