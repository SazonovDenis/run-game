package run.game.testdata.fixture


import jandcode.core.dbm.fixture.*
import run.game.dao.*
import run.game.dao.backstage.*

/**
 *
 */
class Plan_fb extends BaseFixtureBuilder {


    protected void onBuild() {
        createPlanFromFile_spelling("Овощи", "res:run/game/testdata/csv/plan/vegetable.txt", true)
        createPlanFromFile_spelling("Фрукты", "res:run/game/testdata/csv/plan/fruit.txt", true)
        createPlanFromFile_spelling("Материалы", "res:run/game/testdata/csv/plan/material.txt", true)
        createPlanFromFile_spelling("Овощи и фрукты", "res:run/game/testdata/csv/plan/vegetable_fruit.txt", true)
        createPlanFromFile_spelling("Цвета", "res:run/game/testdata/csv/plan/color.txt", true)
        //
        createPlanFromFile_sound("Dumb ways to die", "res:run/game/testdata/csv/plan/dumb.txt", false)
        createPlanFromFile_sound("Овощи (аудио)", "res:run/game/testdata/csv/plan/vegetable.txt", true)
        createPlanFromFile_sound("Фрукты (аудио)", "res:run/game/testdata/csv/plan/fruit.txt", true)
        createPlanFromFile_sound("Материалы (аудио)", "res:run/game/testdata/csv/plan/material.txt", true)
        createPlanFromFile_sound("Овощи и фрукты (аудио)", "res:run/game/testdata/csv/plan/vegetable_fruit.txt", true)
        createPlanFromFile_sound("Цвета (аудио)", "res:run/game/testdata/csv/plan/color.txt", true)
        //
        createPlanFromFile_spelling("Числительные / Саны", "res:run/game/testdata/csv/plan/kz.numbers.txt", true)
        createPlanFromFile_spelling("Продкуты / Азық-түлік", "res:run/game/testdata/csv/plan/kz.products.txt", true)
        createPlanFromFile_sound("Числительные / Саны (аудио)", "res:run/game/testdata/csv/plan/kz.numbers.txt", true)
        createPlanFromFile_sound("Продкуты / Азық-түлік (аудио)", "res:run/game/testdata/csv/plan/kz.products.txt", true)
    }

    void createPlanFromFile_spelling(String planName, String fileName, boolean useSameItemsAsFalse) {
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
        planCreator.createPlan(planName, items, itemsFalse, RgmDbConst.DataType_word_spelling, RgmDbConst.DataType_word_translate, 10)
    }

    void createPlanFromFile_sound(String planName, String fileName, boolean useSameItemsAsFalse) {
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
        planCreator.createPlan(planName, items, itemsFalse, RgmDbConst.DataType_word_sound, RgmDbConst.DataType_word_translate, 5)
    }


}