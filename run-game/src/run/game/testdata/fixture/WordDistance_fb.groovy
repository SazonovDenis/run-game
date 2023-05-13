package run.game.testdata.fixture

import jandcode.core.dbm.fixture.*
import jandcode.core.store.*
import run.game.util.*

/**
 *
 */
class WordDistance_fb extends BaseFixtureBuilder {

    //
    int maxMatchSize = 20

    //
    protected void onBuild() {
        FixtureTable fxWordDistance = fx.table("WordDistance")
        Store stWordDistance = fxWordDistance.getStore()

        // Заполним из наших csv
        RgmCsvUtils utils = mdb.create(RgmCsvUtils)
        utils.addFromCsv(stWordDistance, "res:run/game/testdata/csv/WordDistance.csv")

        //
        long id = 0
        for (StoreRecord rec : stWordDistance) {
            id = id + 1
            //rec.setValue("id", id)
        }
    }

    //
    protected void onBuild1() {
        FixtureTable fxWordDistance = fx.table("WordDistance")
        Store stWordDistance = fxWordDistance.getStore()

        //
        WordDistance_list list = mdb.create(WordDistance_list)
        list.maxMatchSize = maxMatchSize

        //
        list.fillStore(stWordDistance)
    }

}