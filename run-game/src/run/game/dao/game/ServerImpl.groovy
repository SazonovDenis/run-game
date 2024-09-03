package run.game.dao.game

import jandcode.commons.datetime.*
import jandcode.commons.error.*
import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.dao.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import kis.molap.ntbd.model.cubes.*
import run.game.dao.*
import run.game.dao.backstage.*
import run.game.dao.statistic.*

public class ServerImpl extends RgmMdbUtils implements Server {


    private long MAX_FACT_FOR_GAME = 8
    private long MAX_TASK_COUNT_FOR_FACT_IN_GAME = 2
    private long MAX_TASK_COUNT_FOR_FACT_IN_GAME_IF_ERROR = 4
    private long MAX_TASK_FOR_GAME = 10
    private double RATING_DECREASE_FOR_STARRED = 0.1

    private Rnd rnd = new RndImpl()


    @DaoMethod
    public DataBox getGame(long idGame) {
        long idUsr = getContextOrCurrentUsrId()

        //
        return loadAndPrepareGame(idGame, idUsr)
    }


    @DaoMethod
    public DataBox getLastGame() {
        long idUsr = getContextOrCurrentUsrId()

        //
        StoreRecord recGame = mdb.loadQueryRecord(sqlLastGame(), [usr: idUsr], false)
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
        long idUsr = getCurrentUsrId()

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
        long idUsr = getCurrentUsrId()

        // Все незакрытые игры
        Store stGames = mdb.loadQuery(sqlActiveGames(), [usr: idUsr])

        //
        for (StoreRecord recGame : stGames) {
            long idGame = recGame.getLong("id")

            // Закроем игру
            XDateTime dt = XDateTime.now()
            mdb.execQuery(sqlCloseGame(), [game: idGame, dt: dt])

            // Пересчитаем кубы
            long idPlan = recGame.getLong("plan")
            RgmCubeUtils cubeUtils = mdb.create(RgmCubeUtils)
            cubeUtils.cubesRecalc(idUsr, idGame, idPlan)
        }
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
        long idUsr = getCurrentUsrId()
        mdb.insertRec("GameUsr", [usr: idUsr, game: idGame])


        // ---
        // Отберем подходящие факты (самые плохо выученные с учетом статистики пользователя),
        // а по ним - задания на игру.


        // ---
        // Факты


        // Загрузим факты в плане
        Store stPlanFact = mdb.loadQuery(sqlPlanFact(), [plan: idPlan, usr: idUsr])

        // Слегка рандомизируем рейтинг фактов - иначе для фактов без рейтинга
        // (например, для которых никогда не выдавали заданий) рейтинг перестает быть
        // хорошим выбором порядка - от игры к игре будет выбираться список идущих подряд
        // одних тех же слов с нулевым рейтингом - по порядку в sql-запросе, а нам нужно случайно.
        for (StoreRecord recPlanFact : stPlanFact) {
            // Получаем поправку, чтобы получить изменение не более 10%
            double rndSeed = UtCubeRating.RATING_FACT_MAX * rnd.num(0, 1000) / 10000
            // Исказим рейтинг факта
            recPlanFact.setValue("ratingTask", recPlanFact.getDouble("ratingTask") + rndSeed)
        }

        // Откорректируем рейтинг - от фактов, помеченных как "любимые",
        // отнимем немного баллов, чтобы они с большей вероятностью выпадали
        for (StoreRecord recPlanFact : stPlanFact) {
            // Факт помечен как "isKnownBad"?
            if (recPlanFact.getBoolean("isKnownBad")) {
                recPlanFact.setValue("ratingTask", recPlanFact.getDouble("ratingTask") - recPlanFact.getDouble("ratingTask") * RATING_DECREASE_FOR_STARRED)
            }
        }

        // Сортируем факты по рейтингу
        stPlanFact.sort("ratingTask,ratingQuickness")


        // Отберем факты на игру.
        // Начинаем с начала (там самый плохой рейтинг) и выходим
        // при достижении максимального количества фактов.
        Set<String> factsForGame = new HashSet<>()
        for (StoreRecord recPlanFact : stPlanFact) {
            // Скрытые факты не выдаем
            if (recPlanFact.getBoolean("isHidden")) {
                continue
            }

            // Копим факты
            long factQuestion = recPlanFact.getLong("factQuestion")
            long factAnswer = recPlanFact.getLong("factAnswer")
            String key = factQuestion + "_" + factAnswer
            factsForGame.add(key)

            // Набрали фактов сколько нужно
            if (factsForGame.size() >= MAX_FACT_FOR_GAME) {
                break
            }
        }


        // ---
        // Задания


        // Загрузим задания для плана
        Store stPlanTask = mdb.loadQuery(sqlPlanTask(), [plan: idPlan, usr: idUsr])

        // Слегка рандомизируем порядок заданий - иначе всегда будут выходить
        // одни и те же задания - по порядку в sql-запросе, а нам нужно случайно.
        // Кроме того, учет рейтинга факта позволит добавить больше
        // повторных заданий на факты с самым низким рейтингом.
        for (StoreRecord recPlanTask : stPlanTask) {
            // Получаем поправку, чтобы получить изменение не более 10%
            double rndSeed = UtCubeRating.RATING_FACT_MAX * rnd.num(0, 1000) / 10000
            // Исказим рейтинг факта задания
            recPlanTask.setValue("ratingTask", recPlanTask.getDouble("ratingTask") + rndSeed)
        }

        // Сортируем задания по рейтингу факта
        stPlanTask.sort("ratingTask")


        // Для отобранных фактов выберем задания на игру
        Map<String, Integer> factsForGameSelected = new HashMap<>()
        long taskForGameCount = 0
        for (StoreRecord recTask : stPlanTask) {
            long factQuestion = recTask.getLong("factQuestion")
            long factAnswer = recTask.getLong("factAnswer")
            long task = recTask.getLong("task")

            //
            if (task == 0) {
                mdb.outTable(recTask)
                throw new XError("Не найдено задание для фактов, factQuestion: " + factQuestion + ", factAnswer: " + factAnswer + ", plan: " + idPlan)
            }

            //
            String key = factQuestion + "_" + factAnswer

            // Отбираем задания только среди выбранных фактов
            if (!factsForGame.contains(key)) {
                continue
            }

            // Не превышаем максимального количества заданий на каждую пару фактов в одной игре,
            // а то скучно получать задания про одно и то же.
            int tasksForFactSelectedCount = factsForGameSelected.getOrDefault(key, 0)

            // Для такой пары фактов еще можно добавить задание?
            if (tasksForFactSelectedCount >= MAX_TASK_COUNT_FOR_FACT_IN_GAME) {
                continue
            }

            // Запоминаем количество заданий на игру для каждой пары фактов.
            tasksForFactSelectedCount = tasksForFactSelectedCount + 1
            factsForGameSelected.put(key, tasksForFactSelectedCount)

            // Добавляем задание
            mdb.insertRec("GameTask", [
                    game: idGame,
                    usr : idUsr,
                    task: task,
            ])

            // Набрали заданий на игру сколько нужно?
            taskForGameCount = taskForGameCount + 1
            if (taskForGameCount >= MAX_TASK_FOR_GAME) {
                break
            }
        }


        // ---
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
        // Выбираем последнее выданное, но не отвеченное задание
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
        long idUsr = getCurrentUsrId()
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
        // Выбираем, осталось что-нибудь спросить по плану
        long idGame = recGameTask.getLong("game")
        StoreRecord stGameTask = mdb.loadQueryRecord(sqlGameTaskRemainderCount(), [
                game: idGame,
                usr : idUsr,
        ])

        // Закрываем игру, если нечего
        if (stGameTask.getLong("cnt") == 0) {
            closeActiveGame()
            return
        }


        // --- При ошибке - попробуем добавить еще одно задание на эти же факты
        if (taskResult.get("wasFalse") == true || taskResult.get("wasHint") == true) {
            // Выясним пару фактов текущего задания
            long idTask = recGameTask.getLong("task")
            StoreRecord recTask = mdb.loadQueryRecord(sqlTask(), [id: idTask])
            long factQuestion = recTask.getLong("factQuestion")
            long factAnswer = recTask.getLong("factAnswer")

            // Есть/были в игре еще вопросы на эти факты?
            Store stOtherTask = mdb.loadQuery(
                    sqlOtherTask(),
                    [game: idGame, usr: idUsr, factQuestion: factQuestion, factAnswer: factAnswer]
            )
            //
            if (stOtherTask.size() >= MAX_TASK_COUNT_FOR_FACT_IN_GAME_IF_ERROR) {
                // Заданий с такми фактами уже достаточно, новое не нужно
                return
            }

            // Повторим задание
            mdb.insertRec("GameTask",
                    [game: idGame, usr: idUsr, task: idTask]
            )
        }

    }


    @DaoMethod
    DataBox getPlanTasks(long idPlan) {
        DataBox res = new DataBox()

        //
        long idUsr = getContextOrCurrentUsrId()

        //
        Plan_list list = mdb.create(Plan_list)
        StoreRecord recPlanRaw = list.getPlan(idPlan)


        // --- plan

        // План
        StoreRecord recPlan = mdb.createStoreRecord("Plan.server")
        recPlan.setValues(recPlanRaw.getValues())
        //
        if (recPlan.getBoolean("isOwner")) {
            recPlan.setValue("isAllowed", true)
        }


        // --- tasks

        // Факты в плане - список
        Store stPlanFact = mdb.createStore("PlanFact.list")
        mdb.loadQuery(stPlanFact, sqlPlanFactsStatistic(), [usr: idUsr, plan: idPlan])

        // Дополним факты в плане "богатыми" данными для вопроса и ответа
        // plan -> список PlanFact -> PlanFact.factQuestion -> Fact.item - > Fact.* для этого item, но нужного типа
        FactDataLoader ldr = mdb.create(FactDataLoader)
        ldr.fillFactBodyPlan(stPlanFact, idPlan)


        // --- statistic
        StoreRecord recStatistic = loadRecStatistic(recPlanRaw, stPlanFact)


        // ---
        res.put("plan", recPlan)
        res.put("tasks", stPlanFact)
        res.put("statistic", recStatistic.getValues())

        //
        return res
    }


    @DaoMethod
    DataBox getPlanStatistic(long idPlan, XDate dbeg, XDate dend) {
        DataBox res = new DataBox()

        //
        long idUsr = getContextOrCurrentUsrId()

        //
        Plan_list list = mdb.create(Plan_list)
        StoreRecord recPlanRaw = list.getPlan(idPlan)


        // --- plan

        // План
        StoreRecord recPlan = mdb.createStoreRecord("Plan.server")
        recPlan.setValues(recPlanRaw.getValues())
        //
        if (recPlan.getBoolean("isOwner")) {
            recPlan.setValue("isAllowed", true)
        }

        // --- tasks

        // Факты в плане - список
        Store stPlanFact = mdb.createStore("PlanFact.list")
        mdb.loadQuery(stPlanFact, sqlPlanFactsStatistic(), [usr: idUsr, plan: idPlan])


        // --- statistic
        StoreRecord recStatistic = loadRecStatistic(recPlanRaw, stPlanFact)


        // --- statisticByDay
        XDateTime dendParam = dend.addDays(1).toDateTime().addMSec(-1000)
        XDateTime dbegParam = dbeg.toDateTime()
        Statistic_list statisticList = mdb.create(Statistic_list)
        DataBox resStatistic = statisticList.forPlan(idPlan, dbegParam, dendParam)
        Store stSatisticByDay = resStatistic.get("statisticByDay")
        Map mapStatisticPeriod = resStatistic.get("statisticPeriod")


        // ---
        res.put("plan", recPlan)
        res.put("statistic", recStatistic.getValues())
        res.put("statisticPeriod", mapStatisticPeriod)
        res.put("statisticByDay", stSatisticByDay)

        //
        return res
    }


    protected StoreRecord loadActiveGameRec() {
        long idUsr = getCurrentUsrId()
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

    protected StoreRecord loadRecStatistic(StoreRecord recPlanRaw, Store stPlanFact) {
        StoreRecord recStatistic = mdb.createStoreRecord("Plan.statistic.rec")

        // По всем заданиям игры - сумма баллов и т.д.
        recStatistic.setValue("ratingTask", recPlanRaw.getValue("ratingTask"))
        recStatistic.setValue("ratingQuickness", recPlanRaw.getValue("ratingQuickness"))

        // Считаем количество нескрытых и выученных фактов в плане
        int wordCount = 0
        int wordCountLearnedDiff = 0
        for (StoreRecord rec : stPlanFact) {
            if (!rec.getBoolean("isHidden")) {
                wordCount = wordCount + 1
                if (rec.getDouble("ratingTask") == UtCubeRating.RATING_FACT_MAX) {
                    wordCountLearnedDiff = wordCountLearnedDiff + 1
                }
            }
        }
        //
        recStatistic.setValue("wordCount", wordCount)
        recStatistic.setValue("wordCountLearnedDiff", wordCountLearnedDiff)
        // Максимальный балл берем на основе количества нескрытых фактов в плане
        recStatistic.setValue("ratingMax", wordCount * UtCubeRating.RATING_FACT_MAX)


        //
        return recStatistic
    }

    protected DataBox loadAndPrepareGame(long idGame, long idUsr) {
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

        // Дополним ФАКТЫ игры "богатыми" данными вопроса и ответа
        FactDataLoader ldr = mdb.create(FactDataLoader)
        ldr.fillFactBodyPlan(stGameTasks, idPlan)

        // Дополним ЗАДАНИЯ игры "богатыми" данными вопроса и ответа.
        // Разница между ФАКТАМИ игры и ЗАДАНИЯМИ игры заключается в том, что факты игры
        // берутся из фактов плана (таблица PlanFact), а задания игры берутся из реально
        // заданных вопросов (таблица GameTask и связанные с ней через Task записи в TaskQuestion и TaskOption).
        ldr.fillFactBodyGame(stGameTasks, idGame, idUsr)


        // Дополним факты игры статистикой
        Statistic_list statisticList = mdb.create(Statistic_list)
        DataBox resStatisticList = statisticList.forGame(recGame.getDateTime("dbeg"), recGame.getDateTime("dend"))
        Map mapStatistic = resStatisticList.get("statisticPeriod")


        //
        res.put("game", recGame)
        res.put("tasks", stGameTasks)
        res.put("statistic", mapStatistic)

        //
        return res
    }

    protected DataBox loadAndPrepareGame_Short(long idGame, long idUsr) {
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

    protected DataBox loadAndPrepareGameTask(long idGame, StoreRecord recGameTask) {
        DataBox res = new DataBox()

        //
        long idUsr = getCurrentUsrId()


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
        resTask.setValue("factType", recTask.getLong("factTypeQuestion"))

        // Делаем плоскую запись на основе значений задания и их типам
        FactDataLoader ldr = mdb.create(FactDataLoader)

        // Превращаем списки stTaskQuestion и stTaskOption в пару плоских записей (см. Task.fields)
        Store stTaskQuestion = task.get("taskQuestion")
        for (StoreRecord recTaskQuestion : stTaskQuestion) {
            ldr.convertFactToFlatRecord(recTaskQuestion, resTask)
        }

        // Варианты ответа
        Store stTaskOption = task.get("taskOption")
        for (StoreRecord rec : stTaskOption) {
            StoreRecord recTaskOption = resTaskOption.add()
            recTaskOption.setValue("id", rec.getValue("id"))
            recTaskOption.setValue("isTrue", rec.getValue("isTrue"))
            ldr.convertFactToFlatRecord(rec, recTaskOption)
        }


        // ---
        DataBox res = new DataBox()
        res.put("task", resTask)
        res.put("taskOption", resTaskOption)


        //
        return res
    }

    protected StoreRecord choiceGameTask(long idGame) {
        long idUsr = getCurrentUsrId()

        // Извлечем не выданные задания
        Store stGameTask = mdb.loadQuery(sqlChoiceTask(), [
                game: idGame,
                usr : idUsr,
        ])
        // Первое выданное будет впереди, а не отвеченные с конца
        stGameTask.sort("*dtTask")


        // Запомним factQuestion последнего задания (
        long factQuestionLast = stGameTask.get(0).getLong("factQuestion")


        // Узнаем, с какой позиции начинаются не отвеченные задания
        int posTask = Integer.MAX_VALUE
        for (int pos = 0; pos < stGameTask.size(); pos++) {
            StoreRecord recGameTask = stGameTask.get(pos)
            if (recGameTask.isValueNull("dtTask")) {
                posTask = pos
                break
            }
        }

        // Все задания уже отвечены?
        if (posTask > stGameTask.size() - 1) {
            return null
        }

        // Узнаем, есль ли возможность выдать задание, где factQuestion не совпадает
        // с factQuestion последнего задания (factQuestionLast).
        // Такая возможность будет, если в наборе setFactQuestion будет
        // больше одного элемента, что говорит о том, что кроме факта factQuestionLast
        // в невыданных заданиях есть и другие factQuestion
        Set<Long> setFactQuestion = new HashSet<>()
        for (int pos = posTask; pos < stGameTask.size(); pos++) {
            StoreRecord recGameTask = stGameTask.get(pos)
            setFactQuestion.add(recGameTask.getLong("factQuestion"))
        }

        // Выберем случайное
        StoreRecord rec
        while (true) {
            int n = rnd.num(posTask, stGameTask.size() - 1)
            rec = stGameTask.get(n)
            if (rec.getLong("factQuestion") != factQuestionLast || setFactQuestion.size() <= 1) {
                break
            }
        }

        //
        return rec
    }

    protected StoreRecord getGameLastTask(long idGame) {
        long idUsr = getCurrentUsrId()

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


    private String sqlPlanFact() {
        return """ 
-- Все пары фактов в плане
select 
    PlanFact.id, 
    
    PlanFact.factQuestion, 
    PlanFact.factAnswer, 
    
    UsrFact.isHidden,
    UsrFact.isKnownGood,
    UsrFact.isKnownBad,
    
    Cube_UsrFact.ratingTask,
    Cube_UsrFact.ratingQuickness

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

    private String sqlPlanTask() {
        return """ 
-- Подбор всех заданий для каждой пары фактов в плане
select 
    PlanFact.id, 
    
    PlanFact.factQuestion, 
    PlanFact.factAnswer, 
    
    Task.id task,
    
    Cube_UsrFact.ratingTask,
    Cube_UsrFact.ratingQuickness

from
    PlanFact
    left join Task on (
        Task.factQuestion = PlanFact.factQuestion and 
        Task.factAnswer = PlanFact.factAnswer 
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

    private String sqlPlanFactsStatistic() {
        return """ 
-- Пары фактов в плане
select 
    PlanFact.id,
    PlanFact.plan,
    PlanFact.factQuestion,
    PlanFact.factAnswer, 
    
    UsrFact.isHidden,
    UsrFact.isKnownGood,
    UsrFact.isKnownBad,
    
    coalesce(Cube_UsrFact.ratingTask, 0) ratingTask,
    coalesce(Cube_UsrFact.ratingQuickness, 0) ratingQuickness

from
    -- Пара фактов
    PlanFact
    -- Информация пользователя о паре фактов
    left join UsrFact on (
        UsrFact.usr = :usr and
        UsrFact.factQuestion = PlanFact.factQuestion and 
        UsrFact.factAnswer = PlanFact.factAnswer 
    )
    -- Статистическая информация о паре фактов
    left join Cube_UsrFact on (
        Cube_UsrFact.usr = :usr and 
        Cube_UsrFact.factQuestion = PlanFact.factQuestion and 
        Cube_UsrFact.factAnswer = PlanFact.factAnswer
    )
    
where
    PlanFact.plan = :plan    
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
    UsrFact.isKnownBad--,

    --Cube_UsrFact.ratingTask ratingTaskNow,
    --Cube_UsrFact.ratingQuickness ratingQuicknessNow

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
    --left join Cube_UsrFact on (
    --    Cube_UsrFact.usr = :usr and 
    --    Cube_UsrFact.factQuestion = Task.factQuestion and 
    --    Cube_UsrFact.factAnswer = Task.factAnswer
    --)

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
    GameTask.*,
    Task.factQuestion,
    Task.factAnswer
from
    GameTask 
    join Task on (GameTask.task = Task.id)
where
    GameTask.game = :game and
    GameTask.usr = :usr
"""
    }

    private String sqlGameTaskRemainderCount() {
        return """
select
    count(*) cnt
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

    private String sqlTask() {
        return """
select 
    Task.* 
from 
    Task
where
    Task.id = :id 
"""
    }

    private String sqlOtherTask() {
        """
select 
    GameTask.*
from
    GameTask
    join Task on (GameTask.task = Task.id)
where
    GameTask.usr = :usr and
    GameTask.game = :game and
    --GameTask.dtTask is null and
    Task.factQuestion = :factQuestion and
    Task.factAnswer = :factAnswer
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
    Game.dbeg desc,
    Game.id
"""
    }

    private String sqlGameStatistic() {
        return """
select
    Game.*,
    Plan.text planText,
    
    Cube_UsrPlan.count wordCount,
    Cube_UsrPlan.countFull wordCountFull,
    Cube_UsrPlan.countLearned wordCountLearned,
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


}
