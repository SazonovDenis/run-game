package run.game.dao.backstage

import jandcode.commons.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

class Item_list_Test extends RgmBase_Test {


    String word_0 = "warm"
    String word_1 = "синий"
    String word_text = "синий warm up"


    @Test
    void findItems() {
        Store stFact
        Item_list lst = mdb.create(Item_list)


        //
        stFact = lst.findItems(word_0, 0, [:])

        println()
        println("findItems: '" + word_0 + "'")
        mdb.outTable(stFact)


        //
        stFact = lst.findItems(word_1, 0, [:])

        println()
        println("findItems: '" + word_1 + "'")
        mdb.outTable(stFact)


        //
        stFact = lst.findItems(word_text, 0, [:])

        println()
        println("findItems: '" + word_text + "'")
        mdb.outTable(stFact)
    }

    @Test
    void findItems_tag() {
        Map tags = [kaz: true]
        findItems_internal(tags)

        println()
        tags = [eng: true]
        findItems_internal(tags)
    }


    void findItems_internal(Map tags) {
        Store stFact
        Item_list lst = mdb.create(Item_list)


        //
        stFact = lst.findItems(word_0, 0, tags)

        println()
        println("findItems: '" + word_0 + "', tags: " + tags)
        mdb.outTable(stFact)


        //
        stFact = lst.findItems(word_1, 0, tags)

        println()
        println("findItems: '" + word_1 + "', tags: " + tags)
        mdb.outTable(stFact)


        //
        stFact = lst.findItems(word_text, 0, tags)

        println()
        println("findItems: '" + word_text + "', tags: " + tags)
        mdb.outTable(stFact)
    }

    /**
     * Найдем из файла
     */
    @Test
    void findItems_fromFile() {
        String fileName = "test/run/game/dao/backstage/Item_find_Test.txt"

        // Грузим слова из файла
        String text = UtFile.loadString(fileName)

        // Поиск слов среди введенного
        Item_list lst = mdb.create(Item_list)
        Store stItem = lst.findItems(text, 0, [:])

        println()
        println("findTextExact, file: '" + fileName + "'")
        mdb.outTable(stItem)
    }


    @Test
    void findItems_distorted() {
        Store stFact
        Item_list lst = mdb.create(Item_list)

        //
        word_0 = "қасқ"
        word_1 = "каск"


        //
        stFact = lst.findItems(word_0, 0, [:])

        println()
        println("findItems: '" + word_0 + "'")
        mdb.outTable(stFact)


        //
        stFact = lst.findItems(word_1, 0, [:])

        println()
        println("findItems: '" + word_1 + "'")
        mdb.outTable(stFact)
    }


}
