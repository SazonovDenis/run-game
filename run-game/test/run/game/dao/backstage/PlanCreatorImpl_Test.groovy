package run.game.dao.backstage

import jandcode.commons.datetime.*
import org.junit.jupiter.api.*
import run.game.dao.*

class PlanCreatorImpl_Test extends RgmBase_Test {


    def planDefs = [
            [planText: "Овощи", planTextEng: "vegetable", dataTypeQuestion: RgmDbConst.DataType_word_spelling, dataTypeAnswer: RgmDbConst.DataType_word_translate],
            [planText: "Фрукты", planTextEng: "fruit", dataTypeQuestion: RgmDbConst.DataType_word_spelling, dataTypeAnswer: RgmDbConst.DataType_word_translate],
            [planText: "Материалы", planTextEng: "material", dataTypeQuestion: RgmDbConst.DataType_word_spelling, dataTypeAnswer: RgmDbConst.DataType_word_translate],
            [planText: "Цвета", planTextEng: "color", dataTypeQuestion: RgmDbConst.DataType_word_spelling, dataTypeAnswer: RgmDbConst.DataType_word_translate],

            [planText: "Овощи (аудио)", planTextEng: "vegetable-sound", dataTypeQuestion: RgmDbConst.DataType_word_sound, dataTypeAnswer: RgmDbConst.DataType_word_translate],
            [planText: "Фрукты (аудио)", planTextEng: "fruit-sound", dataTypeQuestion: RgmDbConst.DataType_word_sound, dataTypeAnswer: RgmDbConst.DataType_word_translate],
            [planText: "Материалы (аудио)", planTextEng: "material-sound", dataTypeQuestion: RgmDbConst.DataType_word_sound, dataTypeAnswer: RgmDbConst.DataType_word_translate],
            [planText: "Цвета (аудио)", planTextEng: "color-sound", dataTypeQuestion: RgmDbConst.DataType_word_sound, dataTypeAnswer: RgmDbConst.DataType_word_translate],

            [planText: "Числительные / Саны", planTextEng: "kz.numbers", dataTypeQuestion: RgmDbConst.DataType_word_spelling, dataTypeAnswer: RgmDbConst.DataType_word_translate],
            [planText: "Продкуты / Азық-түлік", planTextEng: "kz.products", dataTypeQuestion: RgmDbConst.DataType_word_spelling, dataTypeAnswer: RgmDbConst.DataType_word_translate],
            // [planText: "Числительные / Саны (аудио)", planTextEng: "kz.numbers-sound", dataTypeQuestion: RgmDbConst.DataType_word_sound, dataTypeAnswer: RgmDbConst.DataType_word_translate],
            // [planText: "Продкуты / Азық-түлік (аудио)", planTextEng: "kz.products-sound", dataTypeQuestion: RgmDbConst.DataType_word_sound, dataTypeAnswer: RgmDbConst.DataType_word_translate],
    ]

    @Test
    void text_to_FactsCombinations1() {
        String fileName = "src/run/game/testdata/csv/plan/color.txt"
        String fileNameFactsCombinations = "temp/color.csv"

        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)
        planCreator.text_to_FactsCombinations(
                fileName,
                RgmDbConst.DataType_word_spelling,
                RgmDbConst.DataType_word_translate,
                fileNameFactsCombinations
        )
    }

    @Test
    void text_to_FactsCombinations2() {
        String fileName = "src/run/game/testdata/csv/plan/color-base-sound.txt"
        String fileNameFactsCombinations = "temp/color-base-sound-en-ru.csv"

        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)
        planCreator.text_to_FactsCombinations(
                fileName,
                RgmDbConst.DataType_word_sound,
                RgmDbConst.DataType_word_translate,
                fileNameFactsCombinations
        )
    }

     @Test
    void text_to_FactsCombinations3() {
        String fileName = "src/run/game/testdata/csv/plan/color-sound.txt"
        String fileNameFactsCombinations = "temp/color-sound.csv"

        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)
        planCreator.text_to_FactsCombinations(
                fileName,
                RgmDbConst.DataType_word_sound,
                RgmDbConst.DataType_word_spelling,
                fileNameFactsCombinations
        )
    }

    @Test
    void text_to_FactsCombinations() {
        String fileName = "test/run/game/dao/backstage/PlanCreator_Test.txt"
        String fileNameFactsCombinations = "temp/PlanCreator_FactCombinations.csv"

        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)
        planCreator.text_to_FactsCombinations(
                fileName,
                RgmDbConst.DataType_word_spelling,
                RgmDbConst.DataType_word_translate,
                fileNameFactsCombinations
        )
    }

    @Test
    void factsCombinations_to_Plan() {
        text_to_FactsCombinations()

        //
        String fileName = "temp/PlanCreator_FactCombinations.csv"
        String planName = "Plan #" + XDateTime.now().toString().substring(0, 19).replace(":", "-")

        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)
        planCreator.factsCombinations_to_Plan(planName, fileName)
    }

    @Test
    void factsCombinations_to_Task() {
        text_to_FactsCombinations()

        //
        String fileName = "temp/PlanCreator_FactCombinations.csv"

        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)
        planCreator.factsCombinations_to_Task(fileName)
    }


    @Test
    void text_to_FactsCombinations_list() {
        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)

        //
        for (Map planDef : planDefs) {
            String fileName = "res:run/game/testdata/csv/plan/" + planDef.get("planTextEng") + ".txt"
            String fileNameFactsCombinations = "temp/PlanFact-" + planDef.get("planTextEng") + ".csv"
            long dataTypeQuestion = planDef.get("dataTypeQuestion")
            long dataTypeAnswer = planDef.get("dataTypeAnswer")

            planCreator.text_to_FactsCombinations(fileName, dataTypeQuestion, dataTypeAnswer, fileNameFactsCombinations)
        }

    }


    @Test
    void factsCombinations_to_Plan_list() {
        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)

        //
        for (Map planDef : planDefs) {
            String planName = planDef.get("planText")
            String fileNameFactsCombinations = "temp/PlanFact-" + planDef.get("planTextEng") + ".csv"

            planCreator.factsCombinations_to_Plan(planName, fileNameFactsCombinations)
        }

    }

}
