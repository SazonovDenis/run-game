package run.game.testdata.fixture

import jandcode.core.dbm.fixture.*
import run.game.dao.*
import run.game.dao.backstage.*

/**
 *
 */
class Plan_fb extends BaseFixtureBuilder {


    def planDefs = [
            [planText: "Овощи", planTextEng: "vegetable", dataTypeQuestion: RgmDbConst.DataType_word_spelling, dataTypeAnswer: RgmDbConst.DataType_word_translate],
            [planText: "Фрукты", planTextEng: "fruit", dataTypeQuestion: RgmDbConst.DataType_word_spelling, dataTypeAnswer: RgmDbConst.DataType_word_translate],
            [planText: "Материалы", planTextEng: "material", dataTypeQuestion: RgmDbConst.DataType_word_spelling, dataTypeAnswer: RgmDbConst.DataType_word_translate],
            [planText: "Цвета", planTextEng: "color", dataTypeQuestion: RgmDbConst.DataType_word_spelling, dataTypeAnswer: RgmDbConst.DataType_word_translate],

            [planText: "Овощи (аудио)", planTextEng: "vegetable-sound", dataTypeQuestion: RgmDbConst.DataType_word_sound, dataTypeAnswer: RgmDbConst.DataType_word_translate],
            [planText: "Фрукты (аудио)", planTextEng: "fruit-sound", dataTypeQuestion: RgmDbConst.DataType_word_sound, dataTypeAnswer: RgmDbConst.DataType_word_translate],
            [planText: "Материалы (аудио)", planTextEng: "material-sound", dataTypeQuestion: RgmDbConst.DataType_word_sound, dataTypeAnswer: RgmDbConst.DataType_word_translate],
            [planText: "Цвета (аудио)", planTextEng: "color-sound", dataTypeQuestion: RgmDbConst.DataType_word_sound, dataTypeAnswer: RgmDbConst.DataType_word_translate],
    ]

    protected void onBuild() {
        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)

        //
        for (Map planDef : planDefs) {
            String planName = planDef.get("planText")
            String fileNameFactsCombinations = "res:run/game/testdata/csv/plan/PlanFact-" + planDef.get("planTextEng") + ".csv"

            println("plan: " + planName)
            planCreator.factsCombinations_to_Plan(fileNameFactsCombinations, planName)
        }


/*
        createPlanFromFile("Овощи", "res:run/game/testdata/csv/plan/vegetable.txt")
        createPlanFromFile("Фрукты", "res:run/game/testdata/csv/plan/fruit.txt")
        createPlanFromFile("Материалы", "res:run/game/testdata/csv/plan/material.txt")
        createPlanFromFile("Цвета", "res:run/game/testdata/csv/plan/color.txt")

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
*/
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