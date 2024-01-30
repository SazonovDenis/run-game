package run.game.dao.backstage


import jandcode.commons.datetime.*
import jandcode.core.auth.std.DefaultUserPasswdAuthToken
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

class Backstage_Test extends RgmBase_Test {

    long item = 1030
    String itemText = "cream"
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
                println("Error:")
                println(e.message)
            }
        }
    }


    @Test
    void createPlan() {
        //
        setCurrentUser(new DefaultUserPasswdAuthToken("user1010", null))
        println("CurrentUser: " + authSvc.getCurrentUser().attrs)

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
        // Создаем план (без комбинации фактов)

        //
        Plan_upd planUpd = mdb.create(Plan_upd)
        StoreRecord recPlan = mdb.createStoreRecord("Plan")
        Store stPlanFact = mdb.createStore("PlanFact")
        Store stPlanTag = mdb.createStore("PlanTag")
        //
        String planName = "test-" + XDateTime.now().toString().substring(0, 19).replace(":", "-")
        recPlan.setValue("text", planName)
        stItemFactCombinations.copyTo(stPlanFact)
        stPlanTag.add([tag: RgmDbConst.Tag_access_private])
        //
        planUpd.ins(recPlan, stPlanFact, stPlanTag)
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


            // ---
            // Для каждой комбинации фактов создадим по одному заданию

            //
            for (StoreRecord recFactCombinations : stItemFactCombinations) {
                try {
                    long idFactQuestion = recFactCombinations.getLong("factQuestion")
                    long idFactAnswer = recFactCombinations.getLong("factAnswer")
                    DataBox task = tg.generateTask(idFactQuestion, idFactAnswer, null)

                    //
                    println()
                    printTask(task)

                    taskUpd.saveTask(task)
                } catch (Exception e) {
                    println()
                    println("Error:")
                    println(e.message)
                }
            }
        }

    }


}
