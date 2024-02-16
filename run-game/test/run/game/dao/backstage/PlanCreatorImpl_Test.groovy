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
    ]

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
        String fileName = "temp/PlanCreator_FactCombinations.csv"
        String planName = "Plan #" + XDateTime.now().toString().substring(0, 19).replace(":", "-")

        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)
        planCreator.factsCombinations_to_Plan(fileName, planName)
    }

    @Test
    void factsCombinations_to_Task() {
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

            planCreator.factsCombinations_to_Plan(fileNameFactsCombinations, planName)
        }

    }

}
