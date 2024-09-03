package run.game.dao.backstage

import jandcode.commons.datetime.*
import org.junit.jupiter.api.*
import run.game.dao.*

class PlanCreator_Test extends RgmBase_Test {


    def planDefsViaFactType = [
            //[planTextEng: "color-base", factTypeQuestion: RgmDbConst.FactType_word_spelling, factTypeAnswer: RgmDbConst.FactType_word_translate],
            //[planTextEng: "color-base-sound-en-ru", factTypeQuestion: RgmDbConst.FactType_word_sound, factTypeAnswer: RgmDbConst.FactType_word_translate, combinationLimit: 5],
            //[planTextEng: "color-base-sound-en-en", factTypeQuestion: RgmDbConst.FactType_word_sound, factTypeAnswer: RgmDbConst.FactType_word_spelling, combinationLimit: 5],
            //[planTextEng: "color", factTypeQuestion: RgmDbConst.FactType_word_spelling, factTypeAnswer: RgmDbConst.FactType_word_translate],
            //[planTextEng: "color-sound", factTypeQuestion: RgmDbConst.FactType_word_sound, factTypeAnswer: RgmDbConst.FactType_word_translate, combinationLimit: 5],

            [planTextEng: "kz.products", factTypeQuestion: RgmDbConst.FactType_word_spelling, factTypeAnswer: RgmDbConst.FactType_word_translate, combinationLimit: 5],
            [planTextEng: "kz.products-sound", factTypeQuestion: RgmDbConst.FactType_word_sound, factTypeAnswer: RgmDbConst.FactType_word_translate, combinationLimit: 5],
            [planTextEng: "kz.numbers", factTypeQuestion: RgmDbConst.FactType_word_spelling, factTypeAnswer: RgmDbConst.FactType_word_translate, combinationLimit: 5],
            [planTextEng: "kz.numbers-sound", factTypeQuestion: RgmDbConst.FactType_word_sound, factTypeAnswer: RgmDbConst.FactType_word_translate, combinationLimit: 5],
    ]

    def planDefsViaFile = [
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

        PlanCreator planCreator = mdb.create(PlanCreator)
        planCreator.text_to_FactsCombinations(
                fileName,
                RgmDbConst.FactType_word_spelling,
                RgmDbConst.FactType_word_translate,
                fileNameFactsCombinations
        )
    }

    @Test
    void text_to_FactsCombinations_list() {
        PlanCreator planCreator = mdb.create(PlanCreator)

        //
        for (Map planDef : planDefsViaFactType) {
            String fileName = "res:run/game/testdata/csv/plan/" + planDef.get("planTextEng") + ".txt"
            String fileNameFactsCombinations = "temp/" + planDef.get("planTextEng") + ".csv"
            long factTypeQuestion = planDef.get("factTypeQuestion")
            long factTypeAnswer = planDef.get("factTypeAnswer")
            int combinationLimit = planDef.getOrDefault("combinationLimit", 0)
            planCreator.limitQuestion = combinationLimit
            //
            try {
                println()
                println("fileName: " + fileName + "")
                planCreator.text_to_FactsCombinations(fileName, factTypeQuestion, factTypeAnswer, fileNameFactsCombinations)
            } catch (Exception e) {
                println(e.message)
                //e.printStackTrace()
            }
        }

    }

    @Test
    void factsCombinations_to_Plan() {
        String planName = "Plan #" + XDateTime.now().toString().substring(0, 19).replace(":", "-")
        String fileNameFactsCombinations = "res:run/game/testdata/csv/plan/color-base-ru-en.csv"

        PlanCreator planCreator = mdb.create(PlanCreator)
        planCreator.factsCombinations_to_Plan(planName, fileNameFactsCombinations)
    }


    @Test
    void factsCombinations_to_Plan_list() {
        PlanCreator planCreator = mdb.create(PlanCreator)

        //
        for (Map planDef : planDefsViaFile) {
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

        PlanCreator planCreator = mdb.create(PlanCreator)
        planCreator.factsCombinations_to_Task(fileName)
    }

}
