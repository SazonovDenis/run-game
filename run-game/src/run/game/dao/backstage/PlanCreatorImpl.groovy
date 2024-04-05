package run.game.dao.backstage

import jandcode.commons.*
import jandcode.commons.error.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.util.*

class PlanCreatorImpl extends RgmMdbUtils implements PlanCreator {

    public limitQuestion = 0

    void text_to_FactsCombinations(String fileNameText, long dataTypeQuestion, long dataTypeAnswer, String fileNameFactsCombinations) {
        // ---
        // Загрузим текст
        Item_list itemsList = mdb.create(Item_list)

        //
        String strFile = UtFile.loadString(fileNameText)
        Collection<String> itemsText = strFile.split("\n")


        // ---
        // Найдем сущности по тексту
        Collection<String> wordsNotFound = new HashSet<>()
        Store stItem = itemsList.loadBySpelling(itemsText, wordsNotFound)

        //
        if (wordsNotFound.size() != 0) {
            throw new XError("Не найдены слова: " + wordsNotFound.toString())
        }

        // ---
        // Найдем комбинации фактов для каждой сущности

        //
        Store stFactCombinations = mdb.createStore("FactCombinations")

        //
        for (StoreRecord recItem : stItem) {
            long item = recItem.getLong("id")
            Store stItemFactCombinations = generateItemFactsCombinations(item, dataTypeQuestion, dataTypeAnswer, limitQuestion, 0)
            stItemFactCombinations.copyTo(stFactCombinations)
        }


        // ---
        // Сортируем
        stFactCombinations.sort("itemValue,factValueAnswer,factValueQuestion")


        // ---
        // Сохраним комбинации фактов в файл
        RgmCsvUtils.saveToCsv(stFactCombinations, new File(fileNameFactsCombinations))
    }

    /**
     * Генерит комбинации известных фактов для сущности,
     * ограничивая выборку фактов параметрами dataType и factTag.
     *
     * @param item Сущность
     * @param dataTypeQuestion
     * @param dataTypeAnswer
     * @return Store с комбинациями фактов (factQuestion + factAnswer)
     */
    public Store generateItemFactsCombinations(long item, long dataTypeQuestion, long dataTypeAnswer, int limitQuestion, int limitAnswer) {
        Store res = mdb.createStore("FactCombinations")

        // Загружаем список фактов для "вопроса" и "ответа"
        Fact_list list = mdb.create(Fact_list)
        Store stQuestionLoaded = list.loadFactsByDataType(item, dataTypeQuestion)
        Store stAnswer = list.loadFactsByDataType(item, dataTypeAnswer)

        //
        if (stQuestionLoaded.size() == 0) {
            throw new XError("Не найден dataTypeQuestion: " + dataTypeQuestion + ", item: " + list.loadItem(item).getString("value"))
        }
        if (stAnswer.size() == 0) {
            throw new XError("Не найден dataTypeAnswer: " + dataTypeAnswer + ", item: " + list.loadItem(item).getString("value"))
        }

        // Ограничим количество
        Store stQuestion = mdb.createStore("Fact.list")
        if (limitQuestion > 0) {
            // Нужное число уникальных случайных позиций
            Set<Integer> stPos = new HashSet<>()
            Random rnd = new Random()
            while (stPos.size() < limitQuestion && stPos.size() < stQuestionLoaded.size()) {
                stPos.add(rnd.nextInt(stQuestionLoaded.size()))
            }

            // Наберем записей по случайным позициям
            for (int pos : stPos) {
                StoreRecord rec = stQuestionLoaded.get(pos)
                stQuestion.add(rec)
            }
        } else {
            stQuestionLoaded.copyTo(stQuestion)
        }


        // Перебираем факты: "факт вопрос" и "факт ответ", для каждой пары
        for (StoreRecord recQuestion : stQuestion) {
            for (StoreRecord recAnswer : stAnswer) {
                Map mapFactTagQuestion = new HashMap()
                Map mapFactTagAnswer = new HashMap()
                //

                //
                res.add([
                        item                : recQuestion.getLong("item"),
                        itemValue           : recQuestion.getString("itemValue"),
                        factQuestion        : recQuestion.getLong("id"),
                        factAnswer          : recAnswer.getLong("id"),
                        factValueQuestion   : recQuestion.getString("factValue"),
                        factValueAnswer     : recAnswer.getString("factValue"),
                        factDataTypeQuestion: recQuestion.getLong("factDataType"),
                        factDataTypeAnswer  : recAnswer.getLong("factDataType"),
                        factTagQuestion     : mapFactTagQuestion,
                        factTagAnswer       : mapFactTagAnswer,
                ])
            }
        }


        //
        return res
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
        Fact_list list = mdb.create(Fact_list)
        // В таблице stFactCombinations перечислены пары фактов по значению - найдем их id
        for (StoreRecord recFactCombinations : stFactCombinations) {
            String factValueQuestion = recFactCombinations.getString("factValueQuestion")
            String factValueAnswer = recFactCombinations.getString("factValueAnswer")
            long factDataTypeQuestion = recFactCombinations.getLong("factDataTypeQuestion")
            long factDataTypeAnswer = recFactCombinations.getLong("factDataTypeAnswer")

            // Ищем все item, у которых есть такое значение факта
            Store stFactQuestion = list.loadFactsByValueDataType(factValueQuestion, factDataTypeQuestion)

            // Бывает, что на одно factValueQuestion имеется несколько разных item,
            // например если искать по translate "быстро", то найдется несколько
            // разных spelling ("fast", "quickly"). Поэтому factValueAnswer ищем среди всех вариантов.
            Store stFactAnswer
            for (StoreRecord recFactQuestion : stFactQuestion) {
                long item = recFactQuestion.getLong("item")
                stFactAnswer = list.loadFactsByValueDataType(item, factValueAnswer, factDataTypeAnswer)
                if (stFactAnswer.size() != 0) {
                    break
                }
            }

            //
            if (stFactAnswer.size() == 0) {
                mdb.outTable(recFactCombinations)
                mdb.outTable(stFactQuestion)
                mdb.outTable(stFactAnswer)
                throw new XError("stFactAnswer.size() == 0")
            }

            long factQuestion = stFactQuestion.get(0).getLong("id")
            long factAnswer = stFactAnswer.get(0).getLong("id")

            stPlanFact.add([
                    factQuestion: factQuestion,
                    factAnswer  : factAnswer,
            ])
        }


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
