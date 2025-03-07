package run.game

import jandcode.core.apx.test.*
import org.junit.jupiter.api.*

class MapSize_Test extends Apx_Test {

    int size = 10_000_000

    @Test
    void test1() {
        Random rnd = new Random(934819403)
        Map<String, Long> res = new HashMap<>()

        //
        println()
        printMemoryUsed()
        println()

        //
        for (int i = 0; i < size; i++) {
            String text = rnd.nextLong() + "-" + rnd.nextLong()
            long id = rnd.nextLong()

            //
            res.put(text, id)

            //
            if (i % 1_000_000 == 0) {
                println("text: " + text + ", id: " + id)
            }
        }

        //
        println()
        println("res.count: " + res.size())
        printMemoryUsed()
        println(Math.round(10 * getMemoryUsed() / res.size()) / 10 + " байт/элемент")
        println()

        //
        res.clear()
        Runtime.getRuntime().gc()

        //
        println("After gc")
        printMemoryUsed()
    }

    void printMemoryUsed() {
        println("memory used: " + Math.round(getMemoryUsed() / 1024 / 1024) + " Мб")
    }

    long getMemoryUsed() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
    }


}
