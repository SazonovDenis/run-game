package run.game.dao.backstage

import jandcode.commons.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*
import run.game.dao.backstage.impl.*

class ItemFinder_Test extends RgmBase_Test {

    String word_0 = "warm"
    String word_1 = "синий"
    Collection<String> text = ["синий", "warm", "up"]

    String word_2 = "пар"
    String word_3 = "сыр"

    String word_4 = "liquoric"


    /**
     * Найдем по точному совпадению
     */
    @Test
    void findWordExact() {
        ItemFinder finder = mdb.create(ItemFinder)
        Store stItem

        //
        List<TextPosition> textPositions = new ArrayList<>()

        //
        textPositions.clear()
        textPositions.add(new TextPosition(word_0))
        stItem = finder.findWordExact(textPositions, [:])

        println()
        println("findWordExact: '" + word_0 + "'")
        mdb.outTable(stItem)


        //
        textPositions.clear()
        textPositions.add(new TextPosition(word_1))
        stItem = finder.findWordExact(textPositions, [:])

        println()
        println("findWordExact: '" + word_1 + "'")
        mdb.outTable(stItem)


        //
        textPositions.clear()
        for (String word : text) {
            textPositions.add(new TextPosition(word))
        }
        stItem = finder.findWordExact(textPositions, [:])

        println()
        println("findWordExact: '" + text + "'")
        mdb.outTable(stItem)
    }

    @Test
    void findWordPart_4() {
        ItemFinder finder = mdb.create(ItemFinder)
        Store stItem

        //
        stItem = finder.findWordPart(word_4, [:])

        println()
        println("findWordExact: '" + word_4 + "'")
        mdb.outTable(stItem)
    }


    /**
     * Найдем по фрагменту написания или перевода
     */
    @Test
    void findWordPart() {
        ItemFinder finder = mdb.create(ItemFinder)
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
        ItemFinder finder = mdb.create(ItemFinder)
        Store stFact
        Map<Long, String> tags

        //
        List<TextPosition> textPositions = new ArrayList<>()
        textPositions.add(new TextPosition(word_3))

        //
        tags = [:]
        stFact = finder.findWordExact(textPositions, tags)

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
        stFact = finder.findWordExact(textPositions, tags)

        println()
        println("findItems: '" + word_3 + "', tags: " + tags)
        mdb.outTable(stFact)


        //
        tags = [
                (RgmDbConst.TagType_word_lang)          : RgmDbConst.TagValue_word_lang_kaz,
                (RgmDbConst.TagType_translate_direction): RgmDbConst.TagValue_translate_direction_kaz_rus,
        ]
        stFact = finder.findWordExact(textPositions, tags)

        println()
        println("findItems: '" + word_3 + "', tags: " + tags)
        mdb.outTable(stFact)
    }

    @Test
    void findTextExact_distorted() {
        ItemFinder finder = mdb.create(ItemFinder)
        Store stFact
        Map<Long, String> tags

        //
        word_2 = "қасқыр"
        word_3 = "каскыр"

        //
        List<TextPosition> textPositions = new ArrayList<>()
        textPositions.add(new TextPosition(word_2))

        //
        tags = [:]
        stFact = finder.findWordExact(textPositions, tags)

        println()
        println("findItems: '" + word_2 + "', tags: " + tags)
        mdb.outTable(stFact)


        //
        textPositions.clear()
        textPositions.add(new TextPosition(word_3))
        stFact = finder.findWordExact(textPositions, tags)

        println()
        println("findItems: '" + word_3 + "', tags: " + tags)
        mdb.outTable(stFact)
    }


    @Test
    void findWordPart_distorted() {
        ItemFinder finder = mdb.create(ItemFinder)
        Store stItem

        //
        word_2 = "қасқ"
        word_3 = "каск"

        //
        stItem = finder.findWordPart(word_2, [:])

        println()
        println("findWordPart: '" + word_2 + "'")
        mdb.outTable(stItem)


        //
        stItem = finder.findWordPart(word_3, [:])

        println()
        println("findWordPart: '" + word_3 + "'")
        mdb.outTable(stItem)
    }


    @Test
    void findWordPart_tags() {
        ItemFinder finder = mdb.create(ItemFinder)
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
        ItemFinder finder = mdb.create(ItemFinder)
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
        ItemFinder finder = mdb.create(ItemFinder)
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
        ItemFinder finder = mdb.create(ItemFinder)
        Store stItem = finder.collectItems(textPositions, [:], null)

        println()
        println("findWordExact, file: '" + fileName + "'")
        mdb.outTable(stItem)
    }


}
