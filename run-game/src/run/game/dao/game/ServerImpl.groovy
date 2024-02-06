package run.game.dao.game

import jandcode.commons.*
import jandcode.commons.datetime.*
import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.dao.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import kis.molap.model.coord.*
import kis.molap.model.cube.*
import kis.molap.model.service.*
import kis.molap.model.value.impl.*
import kis.molap.ntbd.model.*
import run.game.dao.*
import run.game.dao.backstage.*
import run.game.util.*

public class ServerImpl extends RgmMdbUtils implements Server {

    private long MAX_TASK_FOR_GAME = 10
    private double RATING_DECREASE_FOR_STARRED = 0.25

    private Rnd rnd = new RndImpl()


    @DaoMethod
    public DataBox getGame(long idGame) {
        long idUsr = getCurrentUserId()

        //
        return loadAndPrepareGame(idGame, idUsr)
    }

    @DaoMethod
    public DataBox getLastGame() {
        long idUsr = getCurrentUserId()
        StoreRecord recGame = mdb.loadQueryRecord(sqlLastGame(), [usr: idUsr], false)

        //
        if (recGame == null) {
            return null
        }

        //
        long idGame = recGame.getLong("id")

        //
        return loadAndPrepareGame(idGame, idUsr)
    }

    @DaoMethod
    public DataBox getActiveGame() {
        long idUsr = getCurrentUserId()

        //
        StoreRecord recGame = loadActiveGameRec()
        if (recGame == null) {
            return null
        }

        //
        long idGame = recGame.getLong("id")

        //
        return loadAndPrepareGame(idGame, idUsr)
    }

    @DaoMethod
    public void closeActiveGame() {
        // Закроем игру
        long idUsr = getCurrentUserId()

        //
        StoreRecord recGame = loadActiveGameRec()
        if (recGame == null) {
            return
        }

        //
        long idGame = recGame.getLong("id")

        //
        XDateTime dt = XDateTime.now()
        mdb.execQuery(sqlCloseGame(), [game: idGame, dt: dt])

        // Пересчитаем кубы
        long idPlan = recGame.getLong("plan")
        cubesRecalc(idUsr, idGame, idPlan)
    }


    protected StoreRecord loadActiveGameRec() {
        long idUsr = getCurrentUserId()
        Store stGames = mdb.loadQuery(sqlActiveGames(), [usr: idUsr])

        //
        if (stGames.size() == 0) {
            return null
        }

        //
        StoreRecord recGame = stGames.get(0)

        //
        return recGame
    }


    @DaoMethod
    public DataBox gameStart(long idPlan) {
        // Добавляем Game
        StoreRecord recGame = mdb.createStoreRecord("Game")
        XDateTime dt = XDateTime.now()
        recGame.setValue("dbeg", dt)
        recGame.setValue("plan", idPlan)


        // ---
        // Game
        long idGame = mdb.insertRec("Game", recGame)


        // ---
        // Добавим участника игры
        long idUsr = getCurrentUserId()
        mdb.insertRec("GameUsr", [usr: idUsr, game: idGame])


        // ---
        // Отберем подходящие задания на игру (с учетом статистики пользователя)

        // Задания для плана
        Store stPlanTasks = mdb.loadQuery(sqlPlanFact_TaskCombinations(), [plan: idPlan, usr: idUsr])

        // Слегка рандомизируем рейтинг - иначе для для фактов без рейтинга (например,
        // для которых никогда не выдавали заданий) рейтинг перестает быть
        // хорошим выбором порядка - получается список идущих подряд одних тех же слов
        // (потому, что задания сортируются по номеру факта, а для одного факта -
        // несколько заданий).
        for (StoreRecord recPlanTask : stPlanTasks) {
            double progressSeed = rnd.num(-1000, 1000) / 10000
            recPlanTask.setValue("ratingTask", recPlanTask.getDouble("ratingTask") + progressSeed)
        }

        // Откорректируем рейтинг - от помеченных как "любимые" отнимем немного баллов,
        // чтобы они с большей вероятностью выпадали
        for (StoreRecord recTask : stPlanTasks) {
            // Факт помечен как "isKnownBad"?
            if (recTask.getBoolean("isKnownBad")) {
                recTask.setValue("rating", recTask.getDouble("rating") - RATING_DECREASE_FOR_STARRED)
            }
        }

        // Сортируем задания по рейтингу
        stPlanTasks.sort("ratingTask,ratingQuickness")
        mdb.outTable(stPlanTasks)

        // Теперь выберем задания на игру
        long taskForGameCount = 0
        for (StoreRecord recTask : stPlanTasks) {
            // Скрытые факты не выдаем
            if (recTask.getBoolean("isHidden")) {
                continue
            }

            //
            mdb.insertRec("GameTask", [
                    game: idGame,
                    usr : idUsr,
                    task: recTask.getLong("task"),
            ])

            //
            taskForGameCount = taskForGameCount + 1
            if (taskForGameCount >= MAX_TASK_FOR_GAME) {
                break
            }
        }


        //
        return loadAndPrepareGame_Short(idGame, idUsr)
    }


    @DaoMethod
    public DataBox choiceTask(long idGame) {
        // Выбираем, что осталось спросить по плану
        StoreRecord recGameTask = choiceGameTask(idGame)

        // Закрываем игру, если нечего
        if (recGameTask == null) {
            closeActiveGame()
        }

        // Записываем факт выдачи задания для пользователя
        if (recGameTask != null) {
            long idGameTask = recGameTask.getLong("id")
            XDateTime dtTask = XDateTime.now()
            mdb.updateRec("GameTask", [id: idGameTask, dtTask: dtTask])
        }

        //
        return loadAndPrepareGameTask(idGame, recGameTask)
    }


    @DaoMethod
    public DataBox currentTask(long idGame) {
        // Выбираем последнее выданное задание
        StoreRecord recGameTask = getGameLastTask(idGame)

        //
        return loadAndPrepareGameTask(idGame, recGameTask)
    }


    /**
     * Записываем результат выполнения задания
     *
     * @param idGameTask id ранее выданного задания GameTask.id)
     * @param taskResult состояние ответа на задание (wasTrue, wasFalse, wasHint, wasSkip)
     */
    @DaoMethod
    public void postTaskAnswer(long idGameTask, Map taskResult) {
        // Загрузим выданное задание. Если задание чужое, то запись
        // загрузится пустой и будет ошибка, что нормально.
        long idUsr = getCurrentUserId()
        StoreRecord recGameTask = mdb.loadQueryRecord(sqlGameTask(), [id: idGameTask, usr: idUsr])


        // Валидация
        if (taskResult.get("wasTrue") != true &&
                taskResult.get("wasFalse") != true &&
                taskResult.get("wasHint") != true &&
                taskResult.get("wasSkip") != true
        ) {
            mdb.validateErrors.addError("Не указан выбранный ответ на задание")
        }

        //
        if (!recGameTask.isValueNull("dtAnswer")) {
            mdb.validateErrors.addError("Ответ на задание уже дан")
        }

        //
        mdb.validateErrors.checkErrors()


        // --- Обновляем задание
        recGameTask.setValue("dtAnswer", XDateTime.now())
        recGameTask.setValue("wasTrue", taskResult.get("wasTrue"))
        recGameTask.setValue("wasFalse", taskResult.get("wasFalse"))
        recGameTask.setValue("wasHint", taskResult.get("wasHint"))
        recGameTask.setValue("wasSkip", taskResult.get("wasSkip"))
        mdb.updateRec("GameTask", recGameTask)


        // --- Игра окончена?
        // Выбираем, что осталось спросить по плану
        long idGame = recGameTask.getLong("game")
        StoreRecord recGameTaskNext = choiceGameTask(idGame)

        // Закрываем игру, если нечего
        if (recGameTaskNext == null) {
            closeActiveGame()
        }
    }


    @DaoMethod
    DataBox getPlanTasks(long idPlan) {
        DataBox res = new DataBox()

        //
        long idUsr = getCurrentUserId()

        //
        StoreRecord recPlanRaw = mdb.loadQueryRecord(sqlPlanStatistic(), [plan: idPlan, usr: idUsr])

        // План
        StoreRecord recPlan = mdb.createStoreRecord("Plan.server")
        recPlan.setValues(recPlanRaw.getValues())

        // Факты в плане - список
        Store stPlanFacts = mdb.createStore("PlanFact.list")
        mdb.loadQuery(stPlanFacts, sqlPlanFactsStatistic(), [plan: idPlan, usr: idUsr])

        // Дополним факты в плане данными вопроса и ответа
        fillTaskBody(stPlanFacts, idPlan)


        // По всем заданиям игры - сумма баллов и т.д.
        StoreRecord recPlanStatistic = mdb.createStoreRecord("Plan.statistic")
        recPlanStatistic.setValue("ratingTask", recPlanRaw.getValue("ratingTask"))
        recPlanStatistic.setValue("ratingQuickness", recPlanRaw.getValue("ratingQuickness"))

        // Максимальный балл берем на основе количества нескрытых заданий в плане
        recPlanStatistic.setValue("ratingMax", recPlanRaw.getValue("count"))


        //
        res.put("plan", recPlan)
        res.put("tasks", stPlanFacts)
        res.put("statistic", recPlanStatistic.getValues())

        //
        return res
    }


    @DaoMethod
    void saveUsrFact(long factQuestion, long factAnswer, Map dataUsrFact) {
        Task_upd upd = mdb.create(Task_upd)
        upd.saveUsrFact(factQuestion, factAnswer, dataUsrFact)
    }

    DataBox loadAndPrepareGame_Short(long idGame, long idUsr) {
        DataBox res = new DataBox()

        // Данные по игре
        StoreRecord recGame = mdb.createStoreRecord("Game.server")
        mdb.loadQueryRecord(recGame, sqlGameStatistic(), [game: idGame, usr: idUsr])
        res.put("game", recGame)

        // Задания игры
        Store stGameTasksResult = mdb.createStore("GameTask")
        mdb.loadQuery(stGameTasksResult, sqlGameTasksStatistic(), [game: idGame, usr: idUsr])
        res.put("tasksResult", stGameTasksResult)

        //
        return res
    }


    DataBox loadAndPrepareGame(long idGame, long idUsr) {
        DataBox res = new DataBox()

        //
        StoreRecord recGameRaw = mdb.loadQueryRecord(sqlGameStatistic(), [game: idGame, usr: idUsr])

        // Игра
        StoreRecord recGame = mdb.createStoreRecord("Game.server")
        recGame.setValues(recGameRaw.getValues())

        //
        long idPlan = recGame.getLong("plan")

        // Задания игры - список + результат ответа на каждое задание
        Store stGameTasks = mdb.createStore("GameTask.list")
        mdb.loadQuery(stGameTasks, sqlGameTasksStatistic(), [game: idGame, usr: idUsr])

        // Дополним ФАКТЫ игры данными вопроса и ответа
        fillTaskBody(stGameTasks, idPlan)

        // Дополним ЗАДАНИЯ игры данными вопроса и ответа
        fillGameTaskBody(stGameTasks, idGame, idUsr)

        // Дополним факты игры статистикой
        fillGameTaskStaistic(stGameTasks, idGame, idUsr)


        // По всем заданиям игры - сумма баллов и т.д.
        StoreRecord recGameStatistic = mdb.createStoreRecord("Game.statistic")
        Map mapStatisticAggretated = getSum(stGameTasks, ["ratingTask", "ratingQuickness", "ratingTaskDiff", "ratingQuicknessDiff"])
        recGameStatistic.setValues(mapStatisticAggretated)

        // Максимальный балл берем на основе количества нескрытых заданий в плане
        recGameStatistic.setValue("ratingMax", recGameRaw.getLong("count"))


        // Сумма заработанных и проигранных баллов - по отдельности
        double ratingTaskInc = 0
        double ratingTaskDec = 0
        for (StoreRecord recRes : stGameTasks) {
            double ratingTaskDiff = recRes.getDouble("ratingTaskDiff")

            // Увеличение рейтинга
            if (ratingTaskDiff > 0) {
                ratingTaskInc = ratingTaskInc + ratingTaskDiff
            }

            // Уменьшение рейтинга
            if (ratingTaskDiff < 0) {
                ratingTaskDec = ratingTaskDec + ratingTaskDiff
            }
        }
        // Прибавка и потери рейтинга
        recGameStatistic.setValue("ratingTaskInc", ratingTaskInc)
        recGameStatistic.setValue("ratingTaskDec", ratingTaskDec)


        //
        res.put("game", recGame)
        res.put("tasks", stGameTasks)
        res.put("statistic", recGameStatistic.getValues())

        //
        return res
    }

    public static Map<String, Double> getSum(Store store, Collection<String> fields) {
        Map<String, Double> res = new HashMap<>()

        for (String field : fields) {
            double sum = StoreUtils.getSum(store, field)
            sum = CubeUtils.discardExtraDigits(sum)
            res.put(field, sum)
        }

        return res
    }


    String sqlFactQuestion() {
        return """
select 
    PlanFact.factQuestion, 
    PlanFact.factAnswer, 
    
    Fact.item,
    Fact.dataType,
    Fact.value

from 
    PlanFact
    -- Факт того типа, который привязан к PlanFact
    join Fact PlanFact_Fact on (
        PlanFact.factQuestion = PlanFact_Fact.id
    )
    -- Факты всех типов данных для Fact.item
    join Fact on (
        PlanFact_Fact.item = Fact.item and
        Fact.dataType in (${RgmDbConst.DataType_word_spelling + "," + RgmDbConst.DataType_word_sound})
    )

where
    PlanFact.plan = :plan 
"""
    }


    String sqlFactAnswer() {
        return """
select 
    PlanFact.factQuestion, 
    PlanFact.factAnswer, 
    
    Fact.dataType,
    Fact.value

from 
    PlanFact
    join Fact on (
        PlanFact.factAnswer = Fact.id
    )

where
    PlanFact.plan = :plan 
"""
    }


    String sqlGameTaskQuestion() {
        return """
select 
    GameTask.*,
    TaskQuestion.dataType,
    TaskQuestion.value

from 
    GameTask
    join TaskQuestion on (
        TaskQuestion.task = GameTask.task
    )

where
    GameTask.usr = :usr and 
    GameTask.game = :game 
"""
    }


    String sqlGameTaskAnswer() {
        return """
select 
    GameTask.*,
    TaskOption.dataType,
    TaskOption.value

from 
    GameTask
    join TaskOption on (
        TaskOption.task = GameTask.task and 
        TaskOption.isTrue = 1 
    )

where
    GameTask.usr = :usr and 
    GameTask.game = :game 
"""
    }


    protected DataBox loadAndPrepareGameTask(long idGame, StoreRecord recGameTask) {
        DataBox res = new DataBox()

        //
        long idUsr = getCurrentUserId()


        // --- Формируем задание
        if (recGameTask != null) {
            // Грузим задание
            long idTask = recGameTask.getLong("task")
            DataBox resTask = loadTask(idTask)
            StoreRecord recTask = resTask.get("task")
            Store taskOption = resTask.get("taskOption")

            // Дополняем задание технической информацией
            long idGameTask = recGameTask.getLong("id")
            XDateTime dtTask = recGameTask.getDateTime("dtTask")
            recTask.setValue("id", idGameTask)
            recTask.setValue("dtTask", dtTask)

            //
            res.put("task", recTask)
            res.put("taskOption", taskOption)
        }


        // --- Добавим данные по игре
        DataBox resGame = loadAndPrepareGame_Short(idGame, idUsr)
        res.put("game", resGame.get("game"))
        res.put("tasksResult", resGame.get("tasksResult"))


        //
        return res
    }

    /**
     * Загружаем задание и преобразуем по требованиям frontend-api.
     */
    protected DataBox loadTask(long idTask) {
        // --- Грузим задание
        Task_upd upd = mdb.create(Task_upd)
        DataBox task = upd.loadTask(idTask)


        // --- Преобразуем задание по требованиям frontend-api
        StoreRecord resTask = mdb.createStoreRecord("Task.server")
        Store resTaskOption = mdb.createStore("TaskOption.server")

        // Основной вопрос задания
        StoreRecord recTask = task.get("task")
        resTask.setValue("task", recTask.getLong("id"))
        resTask.setValue("dataType", recTask.getLong("dataTypeQuestion"))

        // Делаем плоскую запись на основе значений задания и их типам
        // Превращаем списки stTaskQuestion и stTaskOption в пару плоских записей (см. Task.fields)
        Store stTaskQuestion = task.get("taskQuestion")
        for (StoreRecord recTaskQuestion : stTaskQuestion) {
            convertFactToFlatRecord(recTaskQuestion, resTask)
        }

        // Варианты ответа
        Store stTaskOption = task.get("taskOption")
        for (StoreRecord rec : stTaskOption) {
            StoreRecord recTaskOption = resTaskOption.add()
            recTaskOption.setValue("id", rec.getValue("id"))
            recTaskOption.setValue("isTrue", rec.getValue("isTrue"))
            convertFactToFlatRecord(rec, recTaskOption)
        }


        // ---
        DataBox res = new DataBox()
        res.put("task", resTask)
        res.put("taskOption", resTaskOption)


        //
        return res
    }


    /**
     * Запись типа Fact раскладываем в "плоскую" запись.
     * Берем пару полей recTaskSource.dataType+recTaskSource.value и заполняем
     * соответствующее поле (valueSound, valueTranslate, valueSpelling или valuePicture),
     * в зависимости от recTaskSource.dataType.
     */
    void convertFactToFlatRecord(StoreRecord recTaskSource, StoreRecord resTask) {
        // Звук
        if (recTaskSource.getLong("dataType") == RgmDbConst.DataType_word_sound) {
            resTask.setValue("valueSound", recTaskSource.getValue("value"))
        }
        // Текст
        if (recTaskSource.getLong("dataType") == RgmDbConst.DataType_word_translate) {
            resTask.setValue("valueTranslate", recTaskSource.getValue("value"))
        }
        // Текст
        if (recTaskSource.getLong("dataType") == RgmDbConst.DataType_word_spelling) {
            resTask.setValue("valueSpelling", recTaskSource.getValue("value"))
        }
        // Картинка
        if (recTaskSource.getLong("dataType") == RgmDbConst.DataType_word_picture) {
            resTask.setValue("valuePicture", recTaskSource.getValue("value"))
        }
    }

    /**
     * Дополним факты данными вопроса и ответа,
     * т.е. заполним поля question и answer.
     *
     * Расчитываем, что в stTasks есть поля factQuestion и factAnswer.
     */
    void fillTaskBody(Store stTasks, long idPlan) {
        // Данные вопроса и ответа
        Store stFactQuestion = mdb.loadQuery(sqlFactQuestion(), [plan: idPlan])
        Store stFactAnswer = mdb.loadQuery(sqlFactAnswer(), [plan: idPlan])

        // Размажем
        convertFactsToFlatRecord(stFactQuestion, ["factQuestion", "factAnswer"], stTasks, "question")
        convertFactsToFlatRecord(stFactAnswer, ["factQuestion", "factAnswer"], stTasks, "answer")
    }

    void fillGameTaskBody(Store stTasks, long idGame, long idUsr) {
        // Данные вопроса и ответа
        Store stFactQuestion = mdb.loadQuery(sqlGameTaskQuestion(), [game: idGame, usr: idUsr])
        Store stFactAnswer = mdb.loadQuery(sqlGameTaskAnswer(), [game: idGame, usr: idUsr])

        // Размажем
        convertFactsToFlatRecord(stFactQuestion, ["task"], stTasks, "taskQuestion")
        convertFactsToFlatRecord(stFactAnswer, ["task"], stTasks, "taskAnswer")
    }

    void convertFactsToFlatRecord(Store stFactData, Collection<String> keyFields, Store stDest, String fieldDest) {
        Map<Object, List<StoreRecord>> mapFactQuestion = StoreUtils.collectGroupBy_records(stFactData, keyFields)

        //
        for (StoreRecord recTask : stDest) {
            String key = StoreUtils.concatenateFields(recTask, keyFields)

            //
            StoreRecord recTaskQuestion = mdb.createStoreRecord("Task.fields")
            List<StoreRecord> lstQuestion = mapFactQuestion.get(key)
            for (StoreRecord recQuestion : lstQuestion) {
                convertFactToFlatRecord(recQuestion, recTaskQuestion)
            }
            recTask.setValue(fieldDest, recTaskQuestion.getValues())
        }

    }

    void fillGameTaskStaistic(Store stGameTasks, long idGame, long idUsr) {
        // --- Находим запись о статистике игры

        // Грузим запись
        StoreRecord recUsrGameStatistic = mdb.loadQueryRecord(sqlUsrGameStatistic(), [
                usr : idUsr,
                game: idGame,
        ], false)

        // Нет статистики
        if (recUsrGameStatistic == null) {
            return
        }

        // Извлекаем из BLOB
        List<Map> listTaskStatistic = UtJson.fromJson(new String(recUsrGameStatistic.getValue("taskStatistic"), "utf-8"))


        // --- Разносим статистику из BLOB по записям stGameTasks

        // В Map, где ключ - пара фактов (factQuestion+factAnswer)
        Map<String, Map> mapTaskStatistic = new HashMap<>()
        for (Map taskStatistic : listTaskStatistic) {
            long factQuestion = UtCnv.toLong(taskStatistic.get("factQuestion"))
            long factAnswer = UtCnv.toLong(taskStatistic.get("factAnswer"))
            String key = factQuestion + "_" + factAnswer
            //
            mapTaskStatistic.put(key, taskStatistic)
        }

        // Map по записям
        for (StoreRecord recGameTask : stGameTasks) {
            String key = recGameTask.getLong("factQuestion") + "_" + recGameTask.getLong("factAnswer")
            Map taskStatistic = mapTaskStatistic.get(key)
            if (taskStatistic != null) {
                recGameTask.setValue("ratingTask", taskStatistic.get("ratingTask"))
                recGameTask.setValue("ratingQuickness", taskStatistic.get("ratingQuickness"))
                recGameTask.setValue("ratingTaskDiff", taskStatistic.get("ratingTaskDiff"))
                recGameTask.setValue("ratingQuicknessDiff", taskStatistic.get("ratingQuicknessDiff"))
            }
        }
    }


    protected StoreRecord choiceGameTask(long idGame) {
        long idUsr = getCurrentUserId()

        // Извлечем не выданные задания
        Store stGameTask = mdb.loadQuery(sqlChoiceTask(), [
                game: idGame,
                usr : idUsr,
        ])

        if (stGameTask.size() == 0) {
            return null
        }

        // Выберем случайное
        int n = rnd.num(0, stGameTask.size() - 1)
        StoreRecord rec = stGameTask.get(n)

        //
        return rec
    }


    protected StoreRecord getGameLastTask(long idGame) {
        long idUsr = getCurrentUserId()

        // Извлечем выданное, но не отвеченное задание
        Store stGameTask = mdb.loadQuery(sqlGetTask(), [
                game: idGame,
                usr : idUsr,
        ])

        if (stGameTask.size() == 0) {
            return null
        }

        // Выберем последнее
        StoreRecord rec = stGameTask.get(0)

        //
        return rec
    }


    String sqlPlanFact_TaskCombinations() {
        return """ 
-- Подбор всех комбинаций заданий для каждой пары фактов в плане
select 
    PlanFact.id, 
    
    PlanFact.factQuestion, 
    PlanFact.factAnswer, 
    
    Task.id task,
    
    UsrFact.isHidden,
    UsrFact.isKnownGood,
    UsrFact.isKnownBad,
    
    Cube_UsrFact.ratingTask,
    Cube_UsrFact.ratingQuickness

from
    PlanFact
    left join Task on (
        Task.factQuestion = PlanFact.factQuestion and 
        Task.factAnswer = PlanFact.factAnswer 
    )
    left join UsrFact on (
        UsrFact.usr = :usr and
        UsrFact.factQuestion = PlanFact.factQuestion and 
        UsrFact.factAnswer = PlanFact.factAnswer 
    )
    left join Cube_UsrFact on (
        Cube_UsrFact.usr = :usr and 
        Cube_UsrFact.factQuestion = PlanFact.factQuestion and 
        Cube_UsrFact.factAnswer = PlanFact.factAnswer
    )
    
where
    PlanFact.plan = :plan    
"""
    }


    String sqlPlanFactsStatistic() {
        return """ 
-- Пары фактов в плане
select 
    PlanFact.*, 
    
    UsrFact.isHidden,
    UsrFact.isKnownGood,
    UsrFact.isKnownBad,
    
    coalesce(Cube_UsrFact.ratingTask, 0) ratingTask,
    coalesce(Cube_UsrFact.ratingQuickness, 0) ratingQuickness

from
    PlanFact
    left join UsrFact on (
        UsrFact.usr = :usr and
        UsrFact.factQuestion = PlanFact.factQuestion and 
        UsrFact.factAnswer = PlanFact.factAnswer 
    )
    left join Cube_UsrFact on (
        Cube_UsrFact.usr = :usr and 
        Cube_UsrFact.factQuestion = PlanFact.factQuestion and 
        Cube_UsrFact.factAnswer = PlanFact.factAnswer
    )
    
where
    PlanFact.plan = :plan    
"""
    }


    private String sqlUsrGameStatistic() {
        return """
select 
    Cube_UsrGame.*

from
    Cube_UsrGame
    
where
    Cube_UsrGame.usr = :usr and
    Cube_UsrGame.game = :game
"""
    }

    private String sqlGameTasksStatistic() {
        return """
select
    GameTask.*,
    
    Task.factQuestion, 
    Task.factAnswer, 

    UsrFact.isHidden,
    UsrFact.isKnownGood,
    UsrFact.isKnownBad,

    Cube_UsrFact.ratingTask,
    Cube_UsrFact.ratingQuickness

from
    GameTask
    join Task on (
        GameTask.task = Task.id
    )
    left join UsrFact on (
        UsrFact.usr = :usr and
        UsrFact.factQuestion = Task.factQuestion and 
        UsrFact.factAnswer = Task.factAnswer 
    )
    left join Cube_UsrFact on (
        Cube_UsrFact.usr = :usr and 
        Cube_UsrFact.factQuestion = Task.factQuestion and 
        Cube_UsrFact.factAnswer = Task.factAnswer
    )

where
    GameTask.usr = :usr and
    GameTask.game = :game

order by
    GameTask.dtTask,
    GameTask.id
"""
    }


    private String sqlChoiceTask() {
        return """
select
    GameTask.*
from
    GameTask 
where
    game = :game and
    usr = :usr and
    -- не выданное
    dtTask is null 
"""
    }


    private String sqlGetTask() {
        return """
select
    GameTask.*
from
    GameTask 
where
    game = :game and
    usr = :usr and
    -- выданное, но ...
    dtTask is not null and 
    -- ... но неотвеченные
    dtAnswer is null 
order by
    dtTask desc 
"""
    }


    private String sqlGameTask() {
        return """
select 
    * 
from 
    GameTask
where
    GameTask.id = :id and 
    GameTask.usr = :usr 
"""
    }


    private String sqlLastGame() {
        return """
select
    Game.*
from
    Game
    join GameUsr on (Game.id = GameUsr.game)
where
    GameUsr.usr = :usr
order by    
    Game.dbeg desc,
    Game.id desc
limit 1
"""
    }


    private String sqlActiveGames() {
        return """
select
    Game.*
from
    Game
    join GameUsr on (Game.id = GameUsr.game)
where
    Game.dend is null and
    GameUsr.usr = :usr
order by    
    Game.dbeg,
    Game.id
"""
    }

    private String sqlPlanStatistic() {
        return """
select
    Plan.id,
    Plan.id plan,
    Plan.text planText,
    
    Cube_UsrPlan.count,
    Cube_UsrPlan.countFull,
    Cube_UsrPlan.ratingTask,
    Cube_UsrPlan.ratingQuickness

from
    Plan
    left join Cube_UsrPlan on (
        Cube_UsrPlan.usr = :usr and 
        Cube_UsrPlan.plan = Plan.id 
    )

where
    Plan.id = :plan
"""
    }

    private String sqlGameStatistic() {
        return """
select
    Game.*,
    Plan.text planText,
    
    Cube_UsrPlan.count,
    Cube_UsrPlan.countFull,
    Cube_UsrPlan.ratingTask,
    Cube_UsrPlan.ratingQuickness

from
    Game
    join Plan on (Game.plan = Plan.id)
    join GameUsr on (
        Game.id = GameUsr.game and
        GameUsr.usr = :usr 
    )
    left join Cube_UsrPlan on (
        Cube_UsrPlan.usr = :usr and 
        Cube_UsrPlan.plan = Plan.id 
    )

where
    Game.id = :game
"""
    }

    private String sqlCloseGame() {
        return """
update
    Game
set 
    dend = :dt
where
    Game.dend is null and
    Game.id = :game
"""
    }


    void cubesRecalc(long idUsr, long idGame, long idPlan) {
        // Что считаем
        Store stPlanFact = mdb.loadQuery("select factQuestion, factAnswer, :usr usr from PlanFact where plan = :plan", [usr: idUsr, plan: idPlan])
        CoordList coordsUsrFact = createCoords(stPlanFact, ["usr", "factQuestion", "factAnswer"])
        CoordList coordsUsrGame = createCoords([usr: idUsr, game: idGame])
        CoordList coordsUsrPlan = createCoords([usr: idUsr, plan: idPlan])

        //
        cubeRecalc("Cube_UsrFactStatistic", coordsUsrFact)
        cubeRecalc("Cube_UsrGameStatistic", coordsUsrGame)
        cubeRecalc("Cube_UsrPlanStatistic", coordsUsrPlan)
    }


    void cubeRecalc(String cubeName, CoordList coords) {
        // Создаем куб
        CubeService cubeService = getModel().bean(CubeService)
        Cube cube = cubeService.createCube(cubeName, mdb)

        // Результат будем писать в БД
        CalcResultStreamDb res = new CalcResultStreamDb(mdb, cube.getInfo())
        res.open()

        // Расчет и запись
        cube.calc(coords, null, null, res)

        // Запись остатка
        res.close()

    }


    CoordList createCoords(Map values) {
        CoordList coords = CoordList.create()

        Coord coord = Coord.create()
        for (String field : values.keySet()) {
            coord.put(field, values.get(field))
        }
        coords.add(coord)

        return coords
    }

    CoordList createCoords(Store st, ArrayList<String> fields) {
        CoordList coords = CoordList.create()

        for (StoreRecord rec : st) {
            Coord coord = Coord.create()
            for (String field : fields) {
                coord.put(field, rec.getValue(field))
            }
            coords.add(coord)
        }

        return coords
    }

}
