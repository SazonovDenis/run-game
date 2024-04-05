package run.game.dao.backstage

import jandcode.commons.datetime.*
import org.junit.jupiter.api.*
import run.game.dao.*

class PlanCreatorImpl_Test extends RgmBase_Test {


    def planDefs = [
            //[planTextEng: "color-base", dataTypeQuestion: RgmDbConst.DataType_word_spelling, dataTypeAnswer: RgmDbConst.DataType_word_translate],
            //[planTextEng: "color-base-sound-en-ru", dataTypeQuestion: RgmDbConst.DataType_word_sound, dataTypeAnswer: RgmDbConst.DataType_word_translate, combinationLimit: 5],
            //[planTextEng: "color-base-sound-en-en", dataTypeQuestion: RgmDbConst.DataType_word_sound, dataTypeAnswer: RgmDbConst.DataType_word_spelling, combinationLimit: 5],
            //[planTextEng: "color", dataTypeQuestion: RgmDbConst.DataType_word_spelling, dataTypeAnswer: RgmDbConst.DataType_word_translate],
            //[planTextEng: "color-sound", dataTypeQuestion: RgmDbConst.DataType_word_sound, dataTypeAnswer: RgmDbConst.DataType_word_translate, combinationLimit: 5],

            [planTextEng: "kz.products", dataTypeQuestion: RgmDbConst.DataType_word_spelling, dataTypeAnswer: RgmDbConst.DataType_word_translate, combinationLimit: 5],
            [planTextEng: "kz.products-sound", dataTypeQuestion: RgmDbConst.DataType_word_sound, dataTypeAnswer: RgmDbConst.DataType_word_translate, combinationLimit: 5],
            [planTextEng: "kz.numbers", dataTypeQuestion: RgmDbConst.DataType_word_spelling, dataTypeAnswer: RgmDbConst.DataType_word_translate, combinationLimit: 5],
            [planTextEng: "kz.numbers-sound", dataTypeQuestion: RgmDbConst.DataType_word_sound, dataTypeAnswer: RgmDbConst.DataType_word_translate, combinationLimit: 5],
    ]

    def planDefsA = [
            //[planTextEng: "color-base", planText: "Цвета базовые (en-ru) "],
            //[planTextEng: "color-base-sound-en-ru", planText: "Цвета базовые (en-ru, аудио)"],
            //[planTextEng: "color-base-sound-en-en", planText: "Цвета базовые (en-en, аудио)"],
            //[planTextEng: "color", planText: "Цвета (en-ru)"],
            //[planTextEng: "color-sound", planText: "Цвета (en-en, аудио)"],
            [planTextEng: "kz.numbers", planText: "Числа (kz-ru)"],
            [planTextEng: "kz.numbers-sound", planText: "Числа (kz-ru, аудио)"],
            [planTextEng: "kz.products-kz-ru", planText: "Продукты (kz-ru)"],
            [planTextEng: "kz.products-ru-kz", planText: "Продукты (ru-kz)"],
            [planTextEng: "kz.products-sound-kz-ru", planText: "Продукты (kz-ru, аудио)"],
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
    void text_to_FactsCombinations_list() {
        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)

        //
        for (Map planDef : planDefs) {
            String fileName = "res:run/game/testdata/csv/plan/" + planDef.get("planTextEng") + ".txt"
            String fileNameFactsCombinations = "temp/" + planDef.get("planTextEng") + ".csv"
            long dataTypeQuestion = planDef.get("dataTypeQuestion")
            long dataTypeAnswer = planDef.get("dataTypeAnswer")
            int combinationLimit = planDef.getOrDefault("combinationLimit", 0)
            planCreator.limitQuestion = combinationLimit
            //
            try {
                println()
                println("fileName: " + fileName + "")
                planCreator.text_to_FactsCombinations(fileName, dataTypeQuestion, dataTypeAnswer, fileNameFactsCombinations)
            } catch (Exception e) {
                println(e.message)
                //e.printStackTrace()
            }
        }

    }

    @Test
    void factsCombinations_to_Plan() {
        String planName = "Plan #" + XDateTime.now().toString().substring(0, 19).replace(":", "-")
        String fileNameFactsCombinations = "res:run/game/testdata/csv/plan/kz.products-kz-ru.csv"

        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)
        planCreator.factsCombinations_to_Plan(planName, fileNameFactsCombinations)
    }


    @Test
    void factsCombinations_to_Plan_list() {
        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)

        //
        for (Map planDef : planDefsA) {
            String planName = planDef.get("planText")
            String fileNameFactsCombinations = "res:run/game/testdata/csv/plan/" + planDef.get("planTextEng") + ".csv"

            //
            try {
                println()
                println("fileName: " + fileNameFactsCombinations + "")
                planCreator.factsCombinations_to_Plan(planName, fileNameFactsCombinations)
            } catch (Exception e) {
                println(e.message)
                //e.printStackTrace()
            }

        }

    }


    @Test
    void factsCombinations_to_Task() {
        text_to_FactsCombinations()

        //
        String fileName = "temp/PlanCreator_FactCombinations.csv"

        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)
        planCreator.factsCombinations_to_Task(fileName)
    }

}
