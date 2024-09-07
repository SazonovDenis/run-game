package run.game.dao.backstage

import jandcode.commons.*
import jandcode.commons.error.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.util.*

/*
    Создание планов.

    Шаг 1.
    Формироавние заготовки: генерация комбинаций фактов для списка оригинальных (английских)
    слов и запись результата в CSV. При генерации задается тип факта-вопроса и тип факта-ответа.
    Результат - структура "FactCombinations" (с доп. полями для облегчения редактирования)

    Шаг 2.
    Обработка заготовки: фильтрация списка комбинаций (убираем лишнее).
    Сейчас выполняется вручную.

    Шаг 3.
    Формирование плана: генерация заданий по отфильтрованному списку комбинаций,
    с возможным указанием конкретного списка фактов (слов) для выбора неправильных вариантов.
    Результат - окончательный вид PlanFact и генерация Task.
*/

class PlanCreator extends RgmMdbUtils  {

    public limitQuestion = 0

    void text_to_FactsCombinations(String fileNameText, long factTypeQuestion, long factTypeAnswer, String fileNameFactsCombinations) {
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
            Store stItemFactCombinations = generateItemFactsCombinations(item, factTypeQuestion, factTypeAnswer, limitQuestion, 0)
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
     * ограничивая выборку фактов параметрами factType и factTag.
     *
     * @param item Сущность
     * @param factTypeQuestion
     * @param factTypeAnswer
     * @return Store с комбинациями фактов (factQuestion + factAnswer)
     */
    public Store generateItemFactsCombinations(long item, long factTypeQuestion, long factTypeAnswer, int limitQuestion, int limitAnswer) {
        Store res = mdb.createStore("FactCombinations")

        // Загружаем список фактов для "вопроса" и "ответа"
        Fact_list list = mdb.create(Fact_list)
        Store stQuestionLoaded = list.loadBy_item_factType(item, factTypeQuestion)
        Store stAnswer = list.loadBy_item_factType(item, factTypeAnswer)

        //
        if (stQuestionLoaded.size() == 0) {
            throw new XError("Не найден factTypeQuestion: " + factTypeQuestion + ", item: " + list.loadItem(item).getString("value"))
        }
        if (stAnswer.size() == 0) {
            throw new XError("Не найден factTypeAnswer: " + factTypeAnswer + ", item: " + list.loadItem(item).getString("value"))
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
                        factTypeQuestion: recQuestion.getLong("factType"),
                        factTypeAnswer  : recAnswer.getLong("factType"),
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
        generatePlanFact(stFactCombinations, stPlanFact)


        // ---
        // Формируем задания

        // Формируем два задания так, чтобы неправильные ответы брались из элементов плана
        Collection<String> answerFalseValues = stFactCombinations.getUniqueValues("factValueAnswer")
        for (int n = 1; n <= 2; n++) {
            createAndSaveTask(stPlanFact, answerFalseValues)
        }

        // Формируем еще два задания так, чтобы неправильные ответы брались из всех слов
        for (int n = 1; n <= 2; n++) {
            createAndSaveTask(stPlanFact, null)
        }


        // ---
        // Сохраняем план

        //
        Plan_upd planUpd = mdb.create(Plan_upd)
        //
        long planId = planUpd.insPlanInternal(recPlan.getValues(), DataUtils.storeToList(stPlanFact), DataUtils.storeToList(stPlanTag), null)

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

        // PlanFact
        Store stPlanFact = mdb.createStore("PlanFact")
        stFactCombinations.copyTo(stPlanFact)


        // ---
        // Сгенерим задания

        // Формируем задания так, чтобы неправильные ответы брались из элементов плана
        Collection<String> answerFalseValues = stFactCombinations.getUniqueValues("factValueAnswer")
        createAndSaveTask(stPlanFact, answerFalseValues)
    }


    void generatePlanFact(Store stFactCombinations, Store stPlanFact) {
        Fact_list list = mdb.create(Fact_list)
        // В таблице stFactCombinations перечислены пары фактов по значению - найдем их id
        for (StoreRecord recFactCombinations : stFactCombinations) {
            String factValueQuestion = recFactCombinations.getString("factValueQuestion")
            String factValueAnswer = recFactCombinations.getString("factValueAnswer")
            long factTypeQuestion = recFactCombinations.getLong("factTypeQuestion")
            long factTypeAnswer = recFactCombinations.getLong("factTypeAnswer")

            // Ищем все item, у которых есть такое значение факта
            Store stFactQuestion = list.loadBy_value_factType(factTypeQuestion, factValueQuestion)

            //
            if (stFactQuestion.size() == 0) {
                mdb.outTable(recFactCombinations)
                throw new XError("stFactQuestion.size() == 0")
            }

            // Бывает, что на одно factValueQuestion имеется несколько разных item,
            // например если искать по translate "быстро", то найдется несколько
            // разных spelling ("fast", "quickly"). Поэтому factValueAnswer ищем среди всех вариантов.
            // todo А еще если искать по переводу "зеленый", то получим сразу два item: "жасыл" (kz) и "green" (en),
            // что вообще-то требует фильтрации по направлению.
            // Когда будет много языков романской группы - вообще сложно будет
            StoreRecord recFactQuestion = null
            StoreRecord recFactAnswer = null
            for (StoreRecord recFactQuestionSelected : stFactQuestion) {
                long item = recFactQuestionSelected.getLong("item")
                Store stFactAnswer = list.loadBy_item_value_factType(item, factValueAnswer, factTypeAnswer)
                if (stFactAnswer.size() != 0) {
                    recFactQuestion = recFactQuestionSelected
                    recFactAnswer = stFactAnswer.get(0)
                }
            }

            //
            if (recFactAnswer == null) {
                mdb.outTable(recFactCombinations)
                mdb.outTable(stFactQuestion)
                throw new XError("recFactAnswer == null")
            }

            //
            long factQuestion = recFactQuestion.getLong("id")
            long factAnswer = recFactAnswer.getLong("id")

            stPlanFact.add([
                    factQuestion: factQuestion,
                    factAnswer  : factAnswer,
            ])
        }
    }


    void createAndSaveTask(Store stPlanFact, Collection<String> answerFalseValues) {
        // ---
        // Сгенерим задания
        TaskGenerator tg = mdb.create(TaskGeneratorImpl)
        List<DataBox> tasks = new ArrayList<>()

        //
        for (StoreRecord recPlanFact : stPlanFact) {
            long idFactQuestion = recPlanFact.getLong("factQuestion")
            long idFactAnswer = recPlanFact.getLong("factAnswer")
            DataBox task = tg.generateTask(idFactQuestion, idFactAnswer, answerFalseValues)

            //
            tasks.add(task)
        }


        // ---
        // Сохраняем задания
        Task_upd taskUpd = mdb.create(Task_upd)

        //
        for (DataBox task : tasks) {
            taskUpd.saveTask(task)
        }

    }


}
