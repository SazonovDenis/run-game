package run.game.dao.backstage

import jandcode.commons.UtFile
import jandcode.commons.error.*
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
        String strFile = UtFile.loadString(fileNameText)
        Collection<String> itemsText = strFile.split("\n")
        //Collection<String> itemsText = itemsList.readTextFromFile(fileNameText)


        // ---
        // Найдем сущности по тексту
        Collection<String> wordsNotFound = new HashSet<>()
        Store stItem = itemsList.loadBySpelling(itemsText, wordsNotFound)

        //
        if (wordsNotFound.size() != 0) {
            throw new XError("Не найдены слова: " + wordsNotFound.toString())
        }

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


        // ---
        // Сортируем
        stFactCombinations.sort("factQuestion,factAnswer")


        // ---
        // Сохраним комбинации фактов в файл
        RgmCsvUtils.saveToCsv(stFactCombinations, new File(fileNameFactsCombinations))
    }


    long factsCombinations_to_Plan(String planName, String planFactFileName) {
        // ---
        // Загрузим комбинации фактов

        //
        Store stFactCombinations = mdb.createStore("FactCombinations")
        RgmCsvUtils utils = mdb.create(RgmCsvUtils)
        utils.addFromCsv(stFactCombinations, planFactFileName)


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
        long planId = planUpd.insInternal(recPlan.getValues(), DataUtils.storeToList(stPlanFact), DataUtils.storeToList(stPlanTag), null)

        //
        return planId
    }


    void factsCombinations_to_Task(String fileName) {
        // ---
        // Загрузим комбинации фактов

        //
        Store stFactCombinations = mdb.createStore("FactCombinations")
        RgmCsvUtils utils = mdb.create(RgmCsvUtils)
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
