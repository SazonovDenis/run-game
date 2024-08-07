package run.game.dao.backstage

import jandcode.commons.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

class Item_find_Test extends RgmBase_Test {

    String word_0 = "warm"
    String word_1 = "синий"
    Collection<String> text = ["синий", "warm", "up"]

    String word_2 = "пар"
    String word_3 = "сыр"


    /**
     * Найдем по точному совпадению
     */
    @Test
    void findTextExact() {
        Item_find finder = mdb.create(Item_find)
        Store stItem


        //
        stItem = finder.findTextExact([word_0], [:])

        println()
        println("findTextExact: '" + word_0 + "'")
        mdb.outTable(stItem)


        //
        stItem = finder.findTextExact([word_1], [:])

        println()
        println("findTextExact: '" + word_1 + "'")
        mdb.outTable(stItem)


        //
        stItem = finder.findTextExact(text, [:])

        println()
        println("findTextExact: '" + text + "'")
        mdb.outTable(stItem)
    }


    /**
     * Найдем по фрагменту написания или перевода
     */
    @Test
    void findWordPart() {
        Item_find finder = mdb.create(Item_find)
        Store stItem


        //
        stItem = finder.findWordPart(word_0, [:])

        println()
        println("findWordPart: '" + word_0 + "'")
        mdb.outTable(stItem)


        //
        stItem = finder.findWordPart(word_1, [:])

        println()
        println("findWordPart: '" + word_1 + "'")
        mdb.outTable(stItem)
    }


    /**
     * Найдем по фрагменту написания или перевода
     */
    @Test
    void findTextExact_tags() {
        Item_find finder = mdb.create(Item_find)
        Store stFact
        Map tags

        //
        tags = [:]
        stFact = finder.findTextExact([word_3], tags)

        println()
        println("findItems: '" + word_3 + "', tags: " + tags)
        mdb.outTable(stFact)


        //
        tags = [
                (RgmDbConst.TagType_word_lang)               : RgmDbConst.Tag_word_lang_eng,
                (RgmDbConst.TagType_word_translate_direction): RgmDbConst.Tag_word_translate_direction_eng_rus,
        ]
        stFact = finder.findTextExact([word_3], tags)

        println()
        println("findItems: '" + word_3 + "', tags: " + tags)
        mdb.outTable(stFact)


        //
        tags = [
                (RgmDbConst.TagType_word_lang)               : RgmDbConst.Tag_word_lang_kaz,
                (RgmDbConst.TagType_word_translate_direction): RgmDbConst.Tag_word_translate_direction_kaz_rus,
        ]
        stFact = finder.findTextExact([word_3], tags)

        println()
        println("findItems: '" + word_3 + "', tags: " + tags)
        mdb.outTable(stFact)
    }


    @Test
    void findWordPart_tags() {
        Item_find finder = mdb.create(Item_find)
        Store stItem


        //
        Map tags = [:]
        stItem = finder.findWordPart(word_3, tags)

        println()
        println("findWordPart: '" + word_3 + "'")
        mdb.outTable(stItem)


        //
        tags = [
                (RgmDbConst.TagType_word_lang)               : RgmDbConst.Tag_word_lang_eng,
                (RgmDbConst.TagType_word_translate_direction): RgmDbConst.Tag_word_translate_direction_eng_rus,
        ]
        stItem = finder.findWordPart(word_3, tags)

        println()
        println("findWordPart: '" + word_3 + "', tags: " + tags)
        mdb.outTable(stItem)


        //
        tags = [
                (RgmDbConst.TagType_word_lang)               : RgmDbConst.Tag_word_lang_kaz,
                (RgmDbConst.TagType_word_translate_direction): RgmDbConst.Tag_word_translate_direction_kaz_rus,
        ]
        stItem = finder.findWordPart(word_3, tags)

        println()
        println("findWordPart: '" + word_3 + "', tags: " + tags)
        mdb.outTable(stItem)
    }




    /**
     * Найдем из файла
     */
    @Test
    void collectItems_fromFile() {
        String fileName = "test/run/game/dao/backstage/Item_find_Test.txt"

        // Грузим слова из файла
        String text = UtFile.loadString(fileName)

        // Подготовим аргументы
        Map<String, Set> textItems = new LinkedHashMap<>()
        textItems.put(text, null)

        // Поиск слов среди введенного
        Item_find finder = mdb.create(Item_find)
        Store stItem = finder.collectItems(textItems, [:], null)

        println()
        println("findTextExact, file: '" + fileName + "'")
        mdb.outTable(stItem)
    }


}
