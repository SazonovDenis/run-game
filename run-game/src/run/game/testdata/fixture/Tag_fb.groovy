package run.game.testdata.fixture

import jandcode.core.dbm.fixture.*
import jandcode.core.store.*
import run.game.util.*

/**
 *
 */
class Tag_fb extends BaseFixtureBuilder {

    protected void onBuild() {
        FixtureTable fxTag = fx.table("Tag")

        // Заполним из наших csv
        Store stTagCsv = mdb.createStore("Tag.csv")
        RgmCsvUtils utils = mdb.create(RgmCsvUtils)
        utils.addFromCsv(stTagCsv, "res:run/game/testdata/csv/Tag.csv")

        //
        long id = 1000
        Store stTag = fxTag.getStore()
        StoreIndex idxTagType = mdb.loadQuery("select * from TagType").getIndex("code")
        for (StoreRecord recTagCsv : stTagCsv) {
            StoreRecord recTag = stTag.add()
            //
            id = id + 1
            recTag.setValue("id", id)
            recTag.setValue("value", recTagCsv.getValue("value"))
            //
            String tagTypeStr = recTagCsv.getValue("tagType")
            StoreRecord recTagType = idxTagType.get(tagTypeStr)
            recTag.setValue("tagType", recTagType.getLong("id"))
        }
    }

}