package run.game.dao.backstage

import jandcode.commons.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*
import run.game.dao.backstage.impl.*

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
        List<TextPosition> textPositions = new ArrayList<>()

        //
        textPositions.clear()
        textPositions.add(new TextPosition(word_0))
        stItem = finder.findTextExact(textPositions, [:])

        println()
        println("findTextExact: '" + word_0 + "'")
        mdb.outTable(stItem)


        //
        textPositions.clear()
        textPositions.add(new TextPosition(word_1))
        stItem = finder.findTextExact(textPositions, [:])

        println()
        println("findTextExact: '" + word_1 + "'")
        mdb.outTable(stItem)


        //
        textPositions.clear()
        for (String word : text) {
            textPositions.add(new TextPosition(word))
        }
        stItem = finder.findTextExact(textPositions, [:])

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
        Map<Long, String> tags

        //
        List<TextPosition> textPositions = new ArrayList<>()
        textPositions.add(new TextPosition(word_3))

        //
        tags = [:]
        stFact = finder.findTextExact(textPositions, tags)

        println()
        println("findItems: '" + word_3 + "', tags: " + tags)
        mdb.outTable(stFact)


        //
        textPositions.clear()
        textPositions.add(new TextPosition(word_3))
        tags = [
                (RgmDbConst.TagType_word_lang)          : RgmDbConst.TagValue_word_lang_eng,
                (RgmDbConst.TagType_translate_direction): RgmDbConst.TagValue_translate_direction_eng_rus,
        ]
        stFact = finder.findTextExact(textPositions, tags)

        println()
        println("findItems: '" + word_3 + "', tags: " + tags)
        mdb.outTable(stFact)


        //
        tags = [
                (RgmDbConst.TagType_word_lang)          : RgmDbConst.TagValue_word_lang_kaz,
                (RgmDbConst.TagType_translate_direction): RgmDbConst.TagValue_translate_direction_kaz_rus,
        ]
        stFact = finder.findTextExact(textPositions, tags)

        println()
        println("findItems: '" + word_3 + "', tags: " + tags)
        mdb.outTable(stFact)
    }


    @Test
    void findWordPart_tags() {
        Item_find finder = mdb.create(Item_find)
        Store stItem
        Map<Long, String> tags


        //
        tags = [:]
        stItem = finder.findWordPart(word_3, tags)

        println()
        println("findWordPart: '" + word_3 + "', tags: " + tags)
        mdb.outTable(stItem)


        //
        tags = [
                (RgmDbConst.TagType_word_lang)          : RgmDbConst.TagValue_word_lang_eng,
                (RgmDbConst.TagType_translate_direction): RgmDbConst.TagValue_translate_direction_eng_rus,
        ]
        stItem = finder.findWordPart(word_3, tags)

        println()
        println("findWordPart: '" + word_3 + "', tags: " + tags)
        mdb.outTable(stItem)


        //
        tags = [
                (RgmDbConst.TagType_word_lang)          : RgmDbConst.TagValue_word_lang_kaz,
                (RgmDbConst.TagType_translate_direction): RgmDbConst.TagValue_translate_direction_kaz_rus,
        ]
        stItem = finder.findWordPart(word_3, tags)

        println()
        println("findWordPart: '" + word_3 + "', tags: " + tags)
        mdb.outTable(stItem)
    }


    @Test
    void findWordPart_tags_dictionary() {
        Item_find finder = mdb.create(Item_find)
        Store stItem


        //
        Map tags = [:]
        stItem = finder.findWordPart(word_3, tags)

        println()
        println("findWordPart: '" + word_3 + "', tags: " + tags)
        mdb.outTable(stItem)


        //
        tags = [
                (RgmDbConst.TagType_dictionary): RgmDbConst.TagValue_dictionary_full,
        ]
        stItem = finder.findWordPart(word_3, tags)

        println()
        println("findWordPart: '" + word_3 + "', tags: " + tags)
        mdb.outTable(stItem)


        //
        tags = [
                (RgmDbConst.TagType_dictionary): RgmDbConst.TagValue_dictionary_base,
        ]
        stItem = finder.findWordPart(word_3, tags)

        println()
        println("findWordPart: '" + word_3 + "', tags: " + tags)
        mdb.outTable(stItem)
    }


    @Test
    void findWordPart_tags_dictionary_lang() {
        Item_find finder = mdb.create(Item_find)
        Store stItem

        //
        Map tags

        //
        tags = [
                (RgmDbConst.TagType_dictionary)         : RgmDbConst.TagValue_dictionary_base,
                (RgmDbConst.TagType_word_lang)          : RgmDbConst.TagValue_word_lang_eng,
                (RgmDbConst.TagType_translate_direction): RgmDbConst.TagValue_translate_direction_eng_rus,
        ]
        stItem = finder.findWordPart(word_3, tags)

        println()
        println("findWordPart: '" + word_3 + "', tags: " + tags)
        mdb.outTable(stItem)
    }


    /**
     * Найдем все слова из файла
     */
    @Test
    void collectItems_fromFile() {
        String fileName = "test/run/game/dao/backstage/Item_find_Test.txt"

        // Грузим слова из файла
        String text = UtFile.loadString(fileName)

        // Подготовим аргументы
        //
        List<TextPosition> textPositions = new ArrayList<>()
        textPositions.add(new TextPosition(text))

        // Поиск слов среди введенного
        Item_find finder = mdb.create(Item_find)
        Store stItem = finder.collectItems(textPositions, [:], null)

        println()
        println("findTextExact, file: '" + fileName + "'")
        mdb.outTable(stItem)
    }


}
