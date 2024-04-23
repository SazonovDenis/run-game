package run.game.testdata.fixture

import jandcode.core.dbm.fixture.*
import jandcode.core.store.*
import run.game.dao.auth.*
import run.game.util.*

/**
 *
 */
class Usr_fb extends BaseFixtureBuilder {

    protected void onBuild() {
        FixtureTable fxUsr = fx.table("Usr")
        fxUsr.rangeId(2000)

        // Заполним из наших csv
        Store stUsr = mdb.createStore("Usr")
        RgmCsvUtils utils = mdb.create(RgmCsvUtils)
        utils.addFromCsv(stUsr, "res:run/game/testdata/csv/Usr.csv")

        //
        Usr_upd upd = mdb.create(Usr_upd.class)
        for (StoreRecord recUsr : stUsr) {
            upd.ins(recUsr.values)
        }
    }

}