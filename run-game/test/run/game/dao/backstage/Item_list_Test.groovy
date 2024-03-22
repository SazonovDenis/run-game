package run.game.dao.backstage

import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

class Item_list_Test extends RgmBase_Test {


    String itemText_0 = "warm"
    String itemText_1 = "синий"
    Collection<String> itemsText = ["синий", "warm", "up"]


    /**
     * Найдем сущности по фрагменту написания или перевода
     */
    @Test
    void findBySpelling() {
        Item_list itemsList = mdb.create(Item_list)
        Store stItem


        //
        stItem = itemsList.findWord(itemText_0)

        println()
        println("findBySpelling: '" + itemText_0 + "'")
        mdb.outTable(stItem)


        //
        stItem = itemsList.findWord(itemText_1)

        println()
        println("findBySpelling: '" + itemText_1 + "'")
        mdb.outTable(stItem)
    }


    /**
     * Найдем сущности по точному совпадению
     */
    @Test
    void loadBySpelling() {
        Item_list itemsList = mdb.create(Item_list)
        Store stItem


        //
        stItem = itemsList.findText([itemText_0])

        println()
        println("loadBySpelling: '" + itemText_0 + "'")
        mdb.outTable(stItem)


        //
        stItem = itemsList.findText([itemText_1])

        println()
        println("loadBySpelling: '" + itemText_1 + "'")
        mdb.outTable(stItem)


        //
        stItem = itemsList.findText(itemsText)

        println()
        println("loadBySpelling: '" + itemsText + "'")
        mdb.outTable(stItem)
    }


    /**
     * Прочитаем сущности из файла
     */
    @Test
    void readFromFile() {
        Item_list itemsList = mdb.create(Item_list)

        //
        String fileName = "test/run/game/dao/backstage/Item_list_Test.txt"
        Collection<String> itemsText = itemsList.readTextFromFile(fileName)

        println()
        println("loadBySpelling, file: '" + fileName + "'")
        for (String itemText : itemsText) {
            println(itemText)
        }
    }


    /**
     * Найдем сущности из файла
     */
    @Test
    void loadBySpelling_FromFile() {
        Item_list itemsList = mdb.create(Item_list)

        //
        String fileName = "test/run/game/dao/backstage/Item_list_Test.txt"
        Store stItem = itemsList.findTextFromFile(fileName)

        println()
        println("loadBySpelling, file: '" + fileName + "'")
        mdb.outTable(stItem)
    }


}
