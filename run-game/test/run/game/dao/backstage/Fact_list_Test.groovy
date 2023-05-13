package run.game.dao.backstage


import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

class Fact_list_Test extends RgmBase_Test {


    long idFact1 = 1001
    long idFact2 = 1002

    long idItem1 = 214
    long idItem2 = 1030


    @Test
    void loadFact() {
        Fact_list list = mdb.create(Fact_list)

        //
        StoreRecord rec1 = list.loadFact(idFact1)
        StoreRecord rec2 = list.loadFact(idFact2)

        //
        println()
        printFact(rec1)
        println()
        printFact(rec2)
    }


    @Test
    void loadFactsByDataType() {
        Fact_list list = mdb.create(Fact_list)

        //
        Store st1 = list.loadFactsByDataType(idItem2, "word-spelling")
        Store st2 = list.loadFactsByDataType(idItem2, "word-translate")
        Store st3 = list.loadFactsByDataType(idItem2, "word-sound")

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


}
