package run.game.dao.backstage

import jandcode.commons.datetime.*
import jandcode.core.auth.std.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*
import run.game.util.DataUtils

@Disabled
class Backstage_Test extends RgmBase_Test {

    long item = 1030
    String _itemText = "cream"
    String itemText = "emerald"
    Collection<String> itemsText = ["orange", "green", "blue"]


    @Test
    void generateTask() {
        // ---
        // Найдем сущности по тексту


        //
        Item_list itemsList = mdb.create(Item_list)
        Store stItem = itemsList.loadBySpelling([itemText])
        //
        println()
        println("Items by value '" + itemText + "'")
        mdb.outTable(stItem)


        // ---
        // Выберем сущность

        long item = stItem.get(0).getLong("id")

        //
        println()
        println("Item.id: " + item)


        // ---
        // Найдем комбинации фактов сущности

        //
        TaskGenerator tg = mdb.create(TaskGeneratorImpl)
        Store stItemFactCombinations = tg.generateItemFactsCombinations(item, RgmDbConst.DataType_word_spelling, RgmDbConst.DataType_word_translate, null, null)
        //
        println()
        println("ItemFactCombinations: word_spelling -> word_translate")
        mdb.outTable(stItemFactCombinations)


        // ---
        // Для каждой комбинации фактов создадим по одному заданию

        //
        for (StoreRecord rec : stItemFactCombinations) {
            try {
                long idFactQuestion = rec.getLong("factQuestion")
                long idFactAnswer = rec.getLong("factAnswer")
                DataBox task = tg.generateTask(idFactQuestion, idFactAnswer, null)

                //
                println()
                printTask(task)
            } catch (Exception e) {
                println()
                e.printStackTrace()
            }
        }
    }


    @Test
    void createPlan_1000() {
        setCurrentUser(new DefaultUserPasswdAuthToken("admin", "111"))
        println("CurrentUser: " + authSvc.getCurrentUser().attrs)
        //
        createPlan()
    }


    @Test
    void createPlan_1010() {
        setCurrentUser(new DefaultUserPasswdAuthToken("user1010", null))
        println("CurrentUser: " + authSvc.getCurrentUser().attrs)
        //
        createPlan()
    }


    @Test
    void createPlan() {
        // ---
        // Найдем сущности по тексту
        Item_list itemsList = mdb.create(Item_list)
        Store stItem = itemsList.loadBySpelling(itemsText)

        //
        println()
        println("Items by value '" + itemsText + "'")
        mdb.outTable(stItem)


        // ---
        // Выберем сущность

        //
        long item = stItem.get(0).getLong("id")
        //
        println()
        println("Item.id: " + item)


        // ---
        // Найдем комбинации фактов сущности

        //
        TaskGenerator tg = mdb.create(TaskGeneratorImpl)
        Store stItemFactCombinations = tg.generateItemFactsCombinations(item, RgmDbConst.DataType_word_spelling, RgmDbConst.DataType_word_translate, null, null)
        //
        println()
        println("ItemFactCombinations: word_spelling -> word_translate")
        mdb.outTable(stItemFactCombinations)


        // ---
        // Создаем план (без заданий)

        //
        Plan_upd planUpd = mdb.create(Plan_upd)
        StoreRecord recPlan = mdb.createStoreRecord("Plan")
        Store stPlanFact = mdb.createStore("PlanFact")
        Store stPlanTag = mdb.createStore("PlanTag")
        //
        String planName = "test-" + XDateTime.now().toString().substring(0, 19).replace(":", "-")
        recPlan.setValue("text", planName)
        //
        stItemFactCombinations.copyTo(stPlanFact)
        //
        planUpd.ins(recPlan.getValues(), DataUtils.storeToList(stPlanFact), DataUtils.storeToList(stPlanTag))
    }


    @Test
    void cteatePlanBySteps() {
        // ---
        // Найдем сущности по тексту
        Item_list itemsList = mdb.create(Item_list)
        Store stItem = itemsList.loadBySpelling(itemsText)

        //
        println()
        println("Items by value '" + itemsText + "'")
        mdb.outTable(stItem)

        //
        StoreRecord recPlan = mdb.createStoreRecord("Plan")
        Store stPlanFact = mdb.createStore("PlanFact")
        Store stPlanTag = mdb.createStore("PlanTag")


        // ---
        // Найдем комбинации фактов для каждой сущности

        //
        Task_upd taskUpd = mdb.create(Task_upd)
        TaskGenerator tg = mdb.create(TaskGeneratorImpl)
        for (StoreRecord recItem : stItem) {
            long item = recItem.getLong("id")
            Store stItemFactCombinations = tg.generateItemFactsCombinations(item, RgmDbConst.DataType_word_spelling, RgmDbConst.DataType_word_translate, null, null)
            //
            println()
            println("Item: '" + recItem.getValue("value") + "', ItemFactCombinations: word_spelling -> word_translate")
            mdb.outTable(stItemFactCombinations)

            //
            stItemFactCombinations.copyTo(stPlanFact)
        }


        //
        println()
        println("PlanFact:")
        mdb.outTable(stPlanFact)


        // ---
        // Для каждой комбинации фактов создадим по одному заданию

        //
        for (StoreRecord recFactCombinations : stPlanFact) {
            try {
                long idFactQuestion = recFactCombinations.getLong("factQuestion")
                long idFactAnswer = recFactCombinations.getLong("factAnswer")
                DataBox task = tg.generateTask(idFactQuestion, idFactAnswer, null)

                //
                //println()
                //printTask(task)

                taskUpd.saveTask(task)
            } catch (Exception e) {
                println()
                println("Error:")
                println(e.message)
            }
        }


        // ---
        // Создаем план

        //
        Plan_upd planUpd = mdb.create(Plan_upd)
        //
        String planName = "test-" + XDateTime.now().toString().substring(0, 19).replace(":", "-")
        recPlan.setValue("text", planName)
        //
        planUpd.ins(recPlan.getValues(), DataUtils.storeToList(stPlanFact), DataUtils.storeToList(stPlanTag))

    }


}