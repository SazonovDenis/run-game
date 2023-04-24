package run.game.dao.backstage

import jandcode.commons.datetime.*
import jandcode.core.apx.test.*
import jandcode.core.store.*
import org.junit.jupiter.api.*

class UtWordDistance_Test extends Apx_Test {


    @Test
    void jaroWinklerDistanceAll() {
        println("=== " + XDateTime.now())

        //
        int maxMatchSize = 15
        Map<String, Map<String, Double>> distancesAll = new HashMap<>()

        //
        Store st = mdb.loadQuery("select * from Item --limit 1000")

        //
        println("=== " + XDateTime.now())

        //
        for (int i = 0; i < st.size(); i++) {
            String word = st.get(i).getString("value")

            //
            Map<String, Double> distances = UtWordDistance.getJaroWinklerMatch(word, st, maxMatchSize)

            //
            distancesAll.put(word, distances)
        }

        //
        println("=== " + XDateTime.now())

        //
        for (String word : distancesAll.keySet()) {
            Map distances = distancesAll.get(word)
            if (distances.size() < 5) {
                println(word + ", size: " + distances.size())
                println("  " + distancesAll.get(word))
            }
        }

        //
        println("===")


        //
        println("learn")
        println("  " + distancesAll.get("learn"))

        println("dog")
        println("  " + distancesAll.get("dog"))

        println("stop")
        println("  " + distancesAll.get("stop"))

        println("people")
        println("  " + distancesAll.get("people"))

        println("head")
        println("  " + distancesAll.get("head"))

        println("carrot")
        println("  " + distancesAll.get("carrot"))
    }

    @Test
    void jaroWinklerDistance() {
        println("=== " + XDateTime.now())

        //
        Store stDataType = mdb.loadQuery("select * from DataType")
        StoreIndex idxDataType = stDataType.getIndex("code")

        Store st_rus = mdb.loadQuery("select * from Fact where dataType = " + idxDataType.get("word-translate").getLong("id") + " --limit 1000")
        Store st_eng = mdb.loadQuery("select * from Fact where dataType = " + idxDataType.get("word-spelling").getLong("id") + " --limit 1000")

        //
        println("=== " + XDateTime.now())

        //
        int maxMatchSize = 15

        //
        String word = "собака"
        Map<String, Double> distances_1 = UtWordDistance.getJaroWinklerMatch(word, st_rus, maxMatchSize)

        //
        word = "dog"
        Map<String, Double> distances_2 = UtWordDistance.getJaroWinklerMatch(word, st_eng, maxMatchSize)

        //
        println(distances_1)
        println(distances_2)

        //
        println("=== " + XDateTime.now())
    }


}
