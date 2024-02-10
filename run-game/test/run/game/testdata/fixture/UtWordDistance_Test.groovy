package run.game.testdata.fixture

import jandcode.commons.datetime.*
import jandcode.core.apx.test.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

class UtWordDistance_Test extends Apx_Test {


    /**
     * Замер скорости вычисления расстония Джаро-Винклера
     * для всех слов в базе
     */
    @Test
    @Disabled
    void jaroWinklerDistance_all() {
        println("=== " + XDateTime.now())

        //
        int maxMatchSize = 15
        Map<String, Map<String, Double>> distancesAll = new HashMap<>()

        //
        Store st = mdb.loadQuery("select * from Item")

        //
        println("=== start: " + XDateTime.now())

        //
        for (StoreRecord rec : st) {
            String word = rec.getString("value")

            //
            Map<String, Double> distances = UtWordDistance.getJaroWinklerMatch(word, st, maxMatchSize)

            //
            distancesAll.put(word, distances)
        }

        //
        println("=== done:  " + XDateTime.now())

        //
        println()
        println("learn")
        println("  " + distancesAll.get("learn"))

        println()
        println("dog")
        println("  " + distancesAll.get("dog"))

        println()
        println("stop")
        println("  " + distancesAll.get("stop"))

        println()
        println("people")
        println("  " + distancesAll.get("people"))

        println()
        println("head")
        println("  " + distancesAll.get("head"))

        println()
        println("carrot")
        println("  " + distancesAll.get("carrot"))
    }

    //
    String _word_1 = "собака"
    String _word_2 = "dog"

    //
    String word_1 = "изумруд"
    String word_2 = "изумруд"

    @Test
    void jaroWinklerDistance() {
        Store st_rus = mdb.loadQuery("select * from Fact where dataType = " + RgmDbConst.DataType_word_translate + " --limit 1000")
        Store st_eng = mdb.loadQuery("select * from Fact where dataType = " + RgmDbConst.DataType_word_spelling + " --limit 1000")

        //
        println("st_rus.size: " + st_rus.size())
        println("st_eng.size: " + st_eng.size())

        //
        println("=== start: " + XDateTime.now())

        //
        int maxMatchSize = 15

        //
        Map<String, Double> distances_1 = UtWordDistance.getJaroWinklerMatch(word_1, st_rus, maxMatchSize)

        //
        Map<String, Double> distances_2 = UtWordDistance.getJaroWinklerMatch(word_2, st_eng, maxMatchSize)

        //
        println("=== done:  " + XDateTime.now())

        //
        println()
        println(word_1)
        println("  " + distances_1)
        println()
        println(word_2)
        println("  " + distances_2)
    }


}
