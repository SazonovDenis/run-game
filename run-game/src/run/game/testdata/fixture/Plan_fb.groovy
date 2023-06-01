package run.game.testdata.fixture


import jandcode.core.dbm.fixture.*
import run.game.dao.*
import run.game.dao.backstage.*

/**
 *
 */
class Plan_fb extends BaseFixtureBuilder {


    protected void onBuild() {
        createPlanFromFile_spelling("Овощи", "res:run/game/testdata/csv/plan/vegetable.txt")
        createPlanFromFile_spelling("Фрукты", "res:run/game/testdata/csv/plan/fruit.txt")
        createPlanFromFile_spelling("Материалы", "res:run/game/testdata/csv/plan/material.txt")
        createPlanFromFile_spelling("Овощи и фрукты", "res:run/game/testdata/csv/plan/vegetable_fruit.txt")
        createPlanFromFile_spelling("Цвета", "res:run/game/testdata/csv/plan/color.txt")
        //
        createPlanFromFile_sound("Овощи (аудио)", "res:run/game/testdata/csv/plan/vegetable.txt")
        createPlanFromFile_sound("Фрукты (аудио)", "res:run/game/testdata/csv/plan/fruit.txt")
        createPlanFromFile_sound("Материалы (аудио)", "res:run/game/testdata/csv/plan/material.txt")
        createPlanFromFile_sound("Овощи и фрукты (аудио)", "res:run/game/testdata/csv/plan/vegetable_fruit.txt")
        createPlanFromFile_sound("Цвета (аудио)", "res:run/game/testdata/csv/plan/color.txt")

        createPlanFromFile_spelling("Числительные / Саны", "res:run/game/testdata/csv/plan/kz.numbers.txt")
        createPlanFromFile_spelling("Продкуты / Азық-түлік", "res:run/game/testdata/csv/plan/kz.products.txt")
        createPlanFromFile_sound("Числительные / Саны (аудио)", "res:run/game/testdata/csv/plan/kz.numbers.txt")
        createPlanFromFile_sound("Продкуты / Азық-түлік (аудио)", "res:run/game/testdata/csv/plan/kz.products.txt")
    }

    void createPlanFromFile_spelling(String planName, String fileName) {
        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)
        RgmTools rgmTools = mdb.create(RgmTools)

        // Ищем слова из файла среди словарных слов
        Collection<Long> items = new ArrayList<>()
        Collection<String> wordsNotFound = new ArrayList<>()
        rgmTools.loadItemsFromTextFile(fileName, items, wordsNotFound)

        //
        planCreator.createPlan(planName, items, RgmDbConst.DataType_word_spelling, RgmDbConst.DataType_word_translate, 10)
    }

    void createPlanFromFile_sound(String planName, String fileName) {
        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)
        RgmTools rgmTools = mdb.create(RgmTools)

        // Ищем слова из файла среди словарных слов
        Collection<Long> items = new ArrayList<>()
        Collection<String> wordsNotFound = new ArrayList<>()
        rgmTools.loadItemsFromTextFile(fileName, items, wordsNotFound)

        //
        planCreator.createPlan(planName, items, RgmDbConst.DataType_word_sound, RgmDbConst.DataType_word_translate, 5)
    }


}