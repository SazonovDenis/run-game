package run.game.dao.backstage

import jandcode.core.apx.test.*
import jandcode.core.store.*
import org.junit.jupiter.api.*

class Fact_list_Test extends Apx_Test {


    long idFact = 1001

    long idItem1 = 214
    long idItem2 = 1030


    @Test
    void loadFact() {
        Fact_list list = mdb.create(Fact_list)

        //
        StoreRecord rec = list.loadFact(idFact)

        //
        println()
        printFact(rec)
    }


    @Test
    void loadFactsByDataType() {
        Fact_list list = mdb.create(Fact_list)

        //
        Store st1 = list.loadFactsByDataType(idItem1, "word-spelling")
        Store st2 = list.loadFactsByDataType(idItem1, "word-translate")
        Store st3 = list.loadFactsByDataType(idItem1, "word-sound")

        //
        println()
        println("word-spelling")
        printFacts(st1)
        //
        println()
        println("word-translate")
        printFacts(st2)
        //
        println()
        println("word-sound")
        printFacts(st3)
    }

    @Test
    void loadFactByTagValue() {
        Fact_list list = mdb.create(Fact_list)

        //
        Store st = list.loadFactsByTagValue(idItem1, "word-category", "top-100")

        //
        println()
        printFacts(st)
    }


    void printFact(StoreRecord rec) {
        mdb.resolveDicts(rec)
        utils.outTable(rec)
    }

    void printFacts(Store st) {
        mdb.resolveDicts(st)
        utils.outTable(st)
    }


}
