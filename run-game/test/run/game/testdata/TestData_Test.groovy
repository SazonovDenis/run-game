package run.game.testdata

import jandcode.core.apx.test.*
import jandcode.core.dbm.fixture.*
import org.junit.jupiter.api.*
import run.game.testdata.fixture.*

class TestData_Test extends Apx_Test {

    @Test
    public void fill_Tag() throws Exception {
        Fixture fx = new Tag_fb().build(model)
        utils.outTableList(fx.stores, 10)
    }

    @Test
    public void fill_ItemFact() throws Exception {
        ItemFact_fb fb = new ItemFact_fb()
        fb.dirBase = "../data/web-grab/"
        fb.badCsv = "../temp/bad.csv"
        Fixture fx = fb.build(model)
        utils.outTableList(fx.stores, 10)
    }

    @Test
    public void fill_WordSynonym() throws Exception {
        WordSynonym_fb fb = new WordSynonym_fb()
        fb.dirBase = "../data/web-grab/"
        Fixture fx = fb.build(model)
        utils.outTableList(fx.stores, 10)
    }

}
