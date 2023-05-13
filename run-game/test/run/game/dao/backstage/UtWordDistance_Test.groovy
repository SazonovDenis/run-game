package run.game.dao.backstage

import jandcode.commons.datetime.*
import jandcode.core.apx.test.*
import jandcode.core.store.*
import org.junit.jupiter.api.*

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
        //Store st = mdb.loadQuery("select * from Item limit 1000")
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

    @Test
    void jaroWinklerDistance() {
        Store stDataType = mdb.loadQuery("select * from DataType")
        StoreIndex idxDataType = stDataType.getIndex("code")

        Store st_rus = mdb.loadQuery("select * from Fact where dataType = " + idxDataType.get("word-translate").getLong("id") + " --limit 1000")
        Store st_eng = mdb.loadQuery("select * from Fact where dataType = " + idxDataType.get("word-spelling").getLong("id") + " --limit 1000")

        //
        println("=== start: " + XDateTime.now())

        //
        int maxMatchSize = 15

        //
        String word_1 = "собака"
        Map<String, Double> distances_1 = UtWordDistance.getJaroWinklerMatch(word_1, st_rus, maxMatchSize)

        //
        String word_2 = "dog"
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
