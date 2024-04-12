package run.game.util

import jandcode.core.apx.test.*
import jandcode.core.dbm.sql.*
import jandcode.core.store.Store
import org.junit.jupiter.api.*

class StoreLoader_rgm_Test extends Apx_Test {

    @Test
    void t_Csv() {
        String fileName = "test/run/game/util/rgm7651326394220460070.tsv"

        Store stParsedText = mdb.createStore("Tesseract.tsv")
        RgmCsvUtils csvUtils = mdb.create(RgmCsvUtils)
        csvUtils.addFromCsv(stParsedText, fileName)

        mdb.outTable(stParsedText)
    }


}
