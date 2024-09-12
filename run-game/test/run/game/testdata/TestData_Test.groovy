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

        // File file = new File("temp/FactTag.csv")
        // RgmCsvUtils utils = mdb.create(RgmCsvUtils)
        // utils.saveToCsv(fx.stores.get("FactTag"), file)
    }

    @Test
    public void fill_Usr() throws Exception {
        Usr_fb fb = new Usr_fb()
        Fixture fx = fb.build(model)
        utils.outTableList(fx.stores, 10)
    }

    @Test
    public void fill_ItemFactTags() throws Exception {
        ItemFact_BigDict_fb fb = new ItemFact_BigDict_fb()
        fb.dirBase = "../" + fb.dirBase
        Fixture fx = fb.build(model)
        utils.outTableList(fx.stores, 10)
    }

    @Test
    public void fill_Palan() throws Exception {
        Plan_fb fb = new Plan_fb()
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

    @Test
    public void fill_WordDistance() throws Exception {
        utils.logOn()
        WordDistance_fb fb = new WordDistance_fb()
        fb.dirBase = "../data/web-grab/"
        Fixture fx = fb.build(model)
        utils.outTableList(fx.stores, 10)
    }

}
