package run.game.testdata.fixture

import jandcode.core.dbm.fixture.*
import jandcode.core.store.*
import run.game.util.*

import java.nio.file.*

/**
 *
 */
class ItemFactTags_fb extends BaseFixtureBuilder {

    String dirBase = "data/dsl-grab/out/eng-rus/"

    protected void onBuild() {
        FixtureTable fxFact = fx.table("Fact")
        FixtureTable fxFactTag = fx.table("FactTag")
        FixtureTable fxItem = fx.table("Item")
        FixtureTable fxItemTag = fx.table("ItemTag")

        // Заполним из наших csv
        RgmCsvUtils utils = mdb.create(RgmCsvUtils)
        Store stItem = mdb.createStore("Item")
        Store stFact = mdb.createStore("Fact")
        Store stItemTag = mdb.createStore("ItemTag")
        Store stFactTag = mdb.createStore("FactTag")
        utils.addFromCsv(stItem, dirBase + "/Item.csv")
        utils.addFromCsv(stFact, dirBase + "/Fact.csv")
        utils.addFromCsv(stItemTag, dirBase + "/ItemTag.csv")
        utils.addFromCsv(stFactTag, dirBase + "/FactTag.csv")

        //stFact.copyTo(fxFact.getStore())
        //stFactTag.copyTo(fxFactTag.getStore())
        //stItem.copyTo(fxItem.getStore())
        //stItemTag.copyTo(fxItemTag.getStore())


        //
        mdb.execQuery("delete from FactTag")
        mdb.execQuery("delete from ItemTag")
        mdb.execQuery("delete from Fact")
        mdb.execQuery("delete from Item")

        csvToDb("Item", "id,value")
        csvToDb("Fact", "id,item,fact,factType,value")
        csvToDb("ItemTag", "id,item,tagType,tagValue")
        csvToDb("FactTag", "id,fact,tagType,tagValue")

        return

        //
        int n = 0
        for (StoreRecord rec : stItem) {
            mdb.insertRec("Item", rec.values)
            //
            n = n + 1
            if (n % 10000 == 0) {
                println("Item: " + n + "/" + stItem.size())
            }
        }
        n = 0
        for (StoreRecord rec : stFact) {
            mdb.insertRec("Fact", rec.values)
            //
            n = n + 1
            if (n % 10000 == 0) {
                println("Fact: " + n + "/" + stItem.size())
            }
        }
        n = 0
        for (StoreRecord rec : stItemTag) {
            mdb.insertRec("ItemTag", rec.values)
            //
            n = n + 1
            if (n % 10000 == 0) {
                println("ItemTag: " + n + "/" + stItem.size())
            }
        }
        n = 0
        for (StoreRecord rec : stFactTag) {
            mdb.insertRec("FactTag", rec.values)
            //
            n = n + 1
            if (n % 10000 == 0) {
                println("FactTag: " + n + "/" + stItem.size())
            }
        }
    }

    void csvToDb(String tableName, String fields) {
        println(tableName)
        File file = new File(dirBase + tableName + ".csv")
        String dirTemp = System.getProperty("java.io.tmpdir")
        File fileTemp = new File(dirTemp + "/" + tableName + ".csv")
        Files.copy(file.toPath(), fileTemp.toPath(), StandardCopyOption.REPLACE_EXISTING)
        //
        String sqlInsert = "copy " + tableName + " (" + fields + ") from '" + fileTemp.getCanonicalPath() + "'"
        mdb.execQuery(sqlInsert)
    }

}