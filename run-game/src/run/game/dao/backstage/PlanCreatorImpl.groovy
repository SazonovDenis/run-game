package run.game.dao.backstage

import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.util.*

class PlanCreatorImpl extends RgmMdbUtils implements PlanCreator {


    void text_to_FactsCombinations(String fileNameText, long dataTypeQuestion, long dataTypeAnswer, String fileNameFactsCombinations) {
        // ---
        // Загрузим текст
        Item_list itemsList = mdb.create(Item_list)

        //
        Collection<String> itemsText = itemsList.readTextFromFile(fileNameText)


        // ---
        // Найдем сущности по тексту
        Store stItem = itemsList.findText(itemsText)

        //
        //println()
        //println("Items by value '" + itemsText + "'")
        //mdb.outTable(stItem, 5)


        // ---
        // Найдем комбинации фактов для каждой сущности

        //
        Store stFactCombinations = mdb.createStore("FactCombinations")

        //
        TaskGenerator tg = mdb.create(TaskGeneratorImpl)
        for (StoreRecord recItem : stItem) {
            long item = recItem.getLong("id")
            Store stItemFactCombinations = tg.generateItemFactsCombinations(item, dataTypeQuestion, dataTypeAnswer, null, null)

            //
            stItemFactCombinations.copyTo(stFactCombinations)
        }

        //
        //println()
        //println("FactCombinations:")
        //mdb.outTable(stFactCombinations, 15)


        // ---
        // Сохраним комбинации фактов в файл

        //
        //RgmCsvUtils.saveToCsv(stItem, "temp/Item.csv")
        RgmCsvUtils.saveToCsv(stFactCombinations, new File(fileNameFactsCombinations))
    }


    void factsCombinations_to_Plan(String fileName, String planName) {
        // ---
        // Загрузим комбинации фактов

        //
        RgmCsvUtils utils = mdb.create(RgmCsvUtils)

        //
        Store stFactCombinations = mdb.createStore("FactCombinations")
        utils.addFromCsv(stFactCombinations, fileName)


        // ---
        // Заполняем план

        //
        StoreRecord recPlan = mdb.createStoreRecord("Plan")
        Store stPlanFact = mdb.createStore("PlanFact")
        Store stPlanTag = mdb.createStore("PlanTag")

        // Plan
        recPlan.setValue("text", planName)
        recPlan.setValue("isPublic", true)


        // PlanFact
        stFactCombinations.copyTo(stPlanFact)


        // ---
        // Сохраняем план

        //
        Plan_upd planUpd = mdb.create(Plan_upd)
        //
        planUpd.insInternal(recPlan.getValues(), DataUtils.storeToList(stPlanFact), DataUtils.storeToList(stPlanTag), null)
    }


    void factsCombinations_to_Task(String fileName) {
        // ---
        // Загрузим комбинации фактов

        //
        RgmCsvUtils utils = mdb.create(RgmCsvUtils)

        //
        Store stFactCombinations = mdb.createStore("FactCombinations")
        utils.addFromCsv(stFactCombinations, fileName)


        // ---
        // Сгенерим задания

        // PlanFact
        Store stPlanFact = mdb.createStore("PlanFact")
        stFactCombinations.copyTo(stPlanFact)

        //
        TaskGenerator tg = mdb.create(TaskGeneratorImpl)
        List<DataBox> tasks = new ArrayList<>()

        //
        for (StoreRecord recFactCombinations : stPlanFact) {
            long idFactQuestion = recFactCombinations.getLong("factQuestion")
            long idFactAnswer = recFactCombinations.getLong("factAnswer")
            DataBox task = tg.generateTask(idFactQuestion, idFactAnswer, null)

            //
            tasks.add(task)
        }


        // ---
        // Сохраняем задания

        //
        Task_upd taskUpd = mdb.create(Task_upd)

        //
        for (DataBox task : tasks) {
            taskUpd.saveTask(task)
        }
    }


}
