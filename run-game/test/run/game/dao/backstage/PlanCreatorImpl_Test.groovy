package run.game.dao.backstage


import jandcode.core.dbm.std.*
import org.junit.jupiter.api.*
import run.game.dao.*
import run.game.testdata.fixture.*

class PlanCreatorImpl_Test extends RgmBase_Test {


    @Test
    void createTasks() {
        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)
        RgmTools rgmTools = mdb.create(RgmTools)

        // Ищем слова из файла среди словарных слов
        Collection<Long> items = new ArrayList<>()
        Collection<String> wordsNotFound = new ArrayList<>()
        rgmTools.loadItemsFromTextFile("test/run/game/dao/backstage/PlanCreatorImpl_3.txt", items, wordsNotFound)

        //
        println("found: " + items.size())
        println(items)
        println("notFound: " + wordsNotFound.size())
        println(wordsNotFound)

        //
        //DataBox plan = planCreator.createPlan("Robinson", items, RgmDbConst.DataType_word_spelling, RgmDbConst.DataType_word_translate, 3)
        Collection<DataBox> tasks = planCreator.createTasks(items, RgmDbConst.DataType_word_spelling, RgmDbConst.DataType_word_translate, 3)

        //
        println()
        for (DataBox task : tasks) {
            printTaskOneLine(task)
        }
    }

    @Test
    void createPlans() {
        Plan_fb fb = new Plan_fb()

        fb.createPlanFromFile_spelling("vegetable", "test/run/game/dao/backstage/vegetable.txt")
        fb.createPlanFromFile_spelling("fruit", "test/run/game/dao/backstage/fruit.txt")
        fb.createPlanFromFile_spelling("material", "test/run/game/dao/backstage/material.txt")
        fb.createPlanFromFile_spelling("fruit&vegetable", "test/run/game/dao/backstage/vegetable_fruit.txt")
    }

    @Test
    void createPlan_1() {
        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)
        RgmTools rgmTools = mdb.create(RgmTools)

        // Ищем слова из файла среди словарных слов
        Collection<Long> items = new ArrayList<>()
        Collection<String> wordsNotFound = new ArrayList<>()
        rgmTools.loadItemsFromTextFile("test/run/game/dao/backstage/PlanCreatorImpl_2.txt", items, wordsNotFound)

        //
        println("found: " + items.size())
        println(items)
        println("notFound: " + wordsNotFound.size())
        println(wordsNotFound)

        //
        long idPlan = planCreator.createPlan("Alise 1", items, RgmDbConst.DataType_word_spelling, RgmDbConst.DataType_word_translate, 3)

        //
        println("idPlan: " + idPlan)
    }


    @Test
    void createPlan_4() {
        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)
        RgmTools rgmTools = mdb.create(RgmTools)

        // Ищем слова из файла среди словарных слов
        Collection<Long> items = new ArrayList<>()
        Collection<String> wordsNotFound = new ArrayList<>()
        rgmTools.loadItemsFromTextFile("test/run/game/dao/backstage/PlanCreatorImpl_4.txt", items, wordsNotFound)

        //
        println("found: " + items.size())
        println(items)
        println("notFound: " + wordsNotFound.size())
        println(wordsNotFound)

        //
        long idPlan = planCreator.createPlan("Robinson 4", items, RgmDbConst.DataType_word_spelling, RgmDbConst.DataType_word_translate, 3)

        //
        println("idPlan: " + idPlan)
    }


}
