package run.game.testdata.fixture

import groovy.transform.*
import jandcode.core.dbm.fixture.*
import jandcode.core.store.Store
import run.game.dao.*
import run.game.dao.backstage.*

/**
 *
 */
//@TypeChecked
class Plan_fb extends BaseFixtureBuilder {


    protected void onBuild() {
        createPlanFromFile_spelling("Овощи", "res:run/game/testdata/csv/plan/vegetable.txt", true)
        createPlanFromFile_spelling("Фрукты", "res:run/game/testdata/csv/plan/fruit.txt", true)
        createPlanFromFile_spelling("Материалы", "res:run/game/testdata/csv/plan/material.txt", true)
        createPlanFromFile_spelling("Овощи и фрукты", "res:run/game/testdata/csv/plan/vegetable_fruit.txt", true)
        createPlanFromFile_spelling("Цвета", "res:run/game/testdata/csv/plan/color.txt", true)
        //
        long idPlan = createPlanFromFile_sound("Dumb ways to die", "res:run/game/testdata/csv/plan/dumb.txt", false)
        mdb.insertRec("UsrPlan", [plan: idPlan, usr: 1000])
        mdb.deleteRec("PlanTag", [plan: idPlan])

        createPlanFromFile_sound("Овощи (аудио)", "res:run/game/testdata/csv/plan/vegetable.txt", true)
        createPlanFromFile_sound("Фрукты (аудио)", "res:run/game/testdata/csv/plan/fruit.txt", true)
        createPlanFromFile_sound("Материалы (аудио)", "res:run/game/testdata/csv/plan/material.txt", true)
        createPlanFromFile_sound("Овощи и фрукты (аудио)", "res:run/game/testdata/csv/plan/vegetable_fruit.txt", true)
        idPlan = createPlanFromFile_sound("Цвета (аудио)", "res:run/game/testdata/csv/plan/color.txt", true)
        mdb.insertRec("UsrPlan", [plan: idPlan, usr: 1000])
        //
        createPlanFromFile_spelling("Числительные / Саны", "res:run/game/testdata/csv/plan/kz.numbers.txt", true)
        createPlanFromFile_spelling("Продкуты / Азық-түлік", "res:run/game/testdata/csv/plan/kz.products.txt", true)
        createPlanFromFile_sound("Числительные / Саны (аудио)", "res:run/game/testdata/csv/plan/kz.numbers.txt", true)
        createPlanFromFile_sound("Продкуты / Азық-түлік (аудио)", "res:run/game/testdata/csv/plan/kz.products.txt", true)
    }

    /**
     *
     * @param planName
     * @param fileName
     * @param useSameItemsAsFalse =true - использовать слова из плана в качестве вариантов
     * неправильного ответа; =false - варианты неправильного ответа будут подобраны из похожих слов
     */
/*
    long createPlanFromFile_spelling(String planName, String fileName, boolean useSameItemsAsFalse) {
        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)
        Item_list itemList = mdb.create(Item_list)

        // Ищем слова из файла среди словарных слов
        Store items = itemList.loadBySpelling(fileName)

        //
        Collection<Long> itemsFalse = null
        if (useSameItemsAsFalse) {
            itemsFalse = items
        }

        //
        return planCreator.createPlan(planName, items, itemsFalse, RgmDbConst.DataType_word_spelling, RgmDbConst.DataType_word_translate, 10)
    }
*/

/*
    long createPlanFromFile_sound(String planName, String fileName, boolean useSameItemsAsFalse) {
        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)
        RgmTools rgmTools = mdb.create(RgmTools)

        // Ищем слова из файла среди словарных слов
        Collection<Long> items = new ArrayList<>()
        Collection<String> wordsNotFound = new ArrayList<>()
        rgmTools.loadItemsFromTextFile(fileName, items, wordsNotFound)

        //
        Collection<Long> itemsFalse = null
        if (useSameItemsAsFalse) {
            itemsFalse = items
        }

        //
        return planCreator.createPlan(planName, items, itemsFalse, RgmDbConst.DataType_word_sound, RgmDbConst.DataType_word_translate, 5)
    }
*/

}