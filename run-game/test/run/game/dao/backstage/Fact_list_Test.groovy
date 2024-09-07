package run.game.dao.backstage


import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

class Fact_list_Test extends RgmBase_Test {


    long idFact1 = 1001
    long idFact2 = 1002

    long idItem1 = 214
    long idItem2 = 1030


    @Test
    void loadFact() {
        Fact_list list = mdb.create(Fact_list)

        //
        StoreRecord rec1 = list.loadFact(idFact1)
        StoreRecord rec2 = list.loadFact(idFact2)

        //
        println()
        printFact(rec1)
        println()
        printFact(rec2)
    }


    @Test
    void loadItemFactsByFactType() {
        Fact_list list = mdb.create(Fact_list)

        //
        Store st1 = list.loadBy_item_factType(idItem2, RgmDbConst.FactType_word_spelling)
        Store st2 = list.loadBy_item_factType(idItem2, RgmDbConst.FactType_word_translate)
        Store st3 = list.loadBy_item_factType(idItem2, RgmDbConst.FactType_word_sound)

        //
        println()
        println("word-spelling")
        printFacts(st1)
        //
        println()
        println("word-translate")
        printFacts(st2)
        //
        println()
        println("word-sound")
        printFacts(st3)
    }

    @Test
    void loadFactByTagValue() {
        Fact_list list = mdb.create(Fact_list)

        //
        Store st = list.loadFactsByTagValue(idItem1, "top-100", RgmDbConst.TagType_word_category)

        //
        println()
        printFacts(st)
    }

    @Test
    void loadFactsByFactType() {
        // Получаем spelling для всех слов из БД
        Fact_list list = mdb.create(Fact_list.class);
        Store stFactSpelling = list.loadBy_factType(RgmDbConst.FactType_word_spelling, Arrays.asList(RgmDbConst.TagType_word_lang))
        Store stFactTranslate = list.loadBy_factType(RgmDbConst.FactType_word_translate, Arrays.asList(RgmDbConst.TagType_translate_direction))

        //
        println()
        printFacts(stFactSpelling, 15)
        printFacts(stFactTranslate, 15)
    }

}
