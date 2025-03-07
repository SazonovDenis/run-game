package run.game

import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

class SearchEngine_Test extends RgmBase_Test {

    List<SearchIndexEl> data = new ArrayList<>();

    SearchEngine se

    List<String> words = [
            "no-such-word", "сыр", "бал", "dog", "can", "style", "кара", "сырный", "мед", "ядовитый", "zero",
            "огонь", "является", "основной", "фазой", "процесса", "горения", "имеет", "свойство", "к",
            "самораспространению", "по", "затронутым", "им", "другим", "горючим", "материал", "хотя", "среди", "процессов",
            "горения", "химических", "веществ", "бывают", "и", "исключения,", "когда", "вещество", "сгорает", "без", "пламени",

            "a-no-such-word", "сыр", "бал", "dog", "can", "style", "кара", "сырный", "мед", "ядовитый", "zero",
            "огонь", "является", "основной", "фазой", "процесса", "горения", "имеет", "свойство", "к",
            "самораспространению", "по", "затронутым", "им", "другим", "горючим", "материал", "хотя", "среди", "процессов",
            "горения", "химических", "веществ", "бывают", "и", "исключения,", "когда", "вещество", "сгорает", "без", "пламени",

            "b-no-such-word", "сыр", "бал", "dog", "can", "style", "кара", "сырный", "мед", "ядовитый", "zero",
            "роли", "окислителя", "чаще", "всего", "выступает", "кислород,", "но", "могут", "выступать", "и", "другие", "элементы",
    ]

    @Override
    void setUp() throws Exception {
        super.setUp()

        //
        printMemoryUsed()

        //
        sw.start("load")
        //
        load()
        //
        sw.stop("load")
        println("load.duration: " + sw.getDuration("load") + " msec")


        //
        println("data.size: " + data.size())
        printMemoryUsed()
        println()

        //
        se = new SearchEngine()
        se.data = data
    }

    @Test
    void test() {
        execSearch()
    }

    void execSearch() {
        List<SearchIndexEl> res = new ArrayList<>()


        //
        sw.start("search")
        int count = 0
        for (int n = 0; n < 1000; n++) {
            for (String value : words) {
                SearchIndexEl el = findItem(value)
                res.add(el)
                count++
            }
        }
        sw.stop("search")

        //
        int countPrint = 0
        println("res.size: " + res.size())
        for (SearchIndexEl el : res) {
            println(el)
            countPrint++
            if (countPrint > 10) {
                println("...")
                break
            }
        }
        println()

        //
        println("search.count:    " + count)
        println("search.duration: " + sw.getDuration("search") + " msec")
        println("search.rate:     " + Math.round(10000 * sw.getDuration("search") / count) / 10000 + " msec/item")
        println("                 " + Math.round(1000 * count / sw.getDuration("search")) + " items/sec")
    }

    SearchIndexEl findItem(String value) {
        value = value.toLowerCase()

        int pos = se.binarySearch(data, value)
        if (pos != -1) {
            return data.get(pos)
        }

        return null
    }

    void printMemoryUsed() {
        println("memory used: " + Math.round(getMemoryUsed() / 1024 / 1024) + " Мб")
    }

    long getMemoryUsed() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
    }

    void load() {
        //
        Store st = mdb.loadQuery("""
select
  Fact.*
from
  Fact
""")

        for (StoreRecord rec : st) {
            String value = rec.getString("factValue")
            value = value.toLowerCase()
            SearchIndexEl el = new SearchIndexEl(value)
            data.add(el)
        }

        data.sort((a, b) -> {
            return a.value.compareTo(b.value)
        })
    }

}
