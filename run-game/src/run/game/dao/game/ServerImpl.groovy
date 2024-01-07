package run.game.dao.game

import jandcode.commons.*
import jandcode.commons.datetime.*
import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.dao.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.dao.backstage.*
import run.game.util.*

public class ServerImpl extends RgmMdbUtils implements Server {

    private long MAX_TASK_FOR_GAME = 10

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
        Store stGames = mdb.loadQuery(sqlActiveGames(), [usr: idUsr])

        //
        if (stGames.size() == 0) {
            return null
        }

        //
        StoreRecord recGame = stGames.get(0)
        long idGame = recGame.getLong("id")

        //
        return loadAndPrepareGame(idGame, idUsr)
    }

    @DaoMethod
    public void closeActiveGame() {
        long idUsr = getCurrentUserId()
        XDateTime dt = XDateTime.now()
        mdb.execQuery(sqlCloseActiveGame(), [usr: idUsr, dt: dt])
    }


    @DaoMethod
    public DataBox gameStart(long idPlan) {
        // Добавляем Game
        StoreRecord recGame = mdb.createStoreRecord("Game")
        XDateTime dt = XDateTime.now()
        recGame.setValue("dbeg", dt)
        recGame.setValue("plan", idPlan)

        //
        long idGame = mdb.insertRec("Game", recGame)


        // Добавим участника игры
        long idUsr = getCurrentUserId()
        mdb.insertRec("GameUsr", [usr: idUsr, game: idGame])


        // Отберем подходящие задания на игру.
        // Задание выбирается с учетом статистики пользователя.
        StatisticManager1 statisticManager = mdb.create(StatisticManager1)
        Store stTask = statisticManager.getStatisticForGame(idGame)
        //stTask.sort("rating")

        // Слегка рандомизируем рейтинг -
        // иначе для для заданий без рейтинга (например, которые никогда не выдавали)
        // рейтинг перестает быть хорошим выбором порядка - получается список идущих подряд
        // одних тех же слов (потому, что задания сортируются по номеру факта,
        // а для одного факта несколько заданий)
        for (StoreRecord recTask : stTask) {
            double progressSeed = rnd.num(-1000, 1000) / 10000
            recTask.setValue("rating", recTask.getDouble("rating") + progressSeed)
        }

        // Теперь выберем задания на игру по рейтингу
        stTask.sort("rating")
        //
        long taskForGameCount = 0
        for (StoreRecord recTask : stTask) {
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


    /**
     * Список уровней с рейтингом,
     * сортированный по некоторому критерию, например по самому отстающему
     * или по запланированному учителем.
     */
    @DaoMethod
    Store getPlans() {
        //Store res = mdb.createStore("Plan.list")
        Store res = mdb.createStore("Plan.list.statistic")

        //
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)
        Store st = statisticManager.getPlansStatistic()

        //
        st.copyTo(res)
        for (StoreRecord rec : res) {
            rec.setValue("tasksStatistic", UtJson.fromJson(rec.getString("tasksStatistic")))
        }

        //
        res.sort("progress")

        //
        return res
    }

    @DaoMethod
    Store getPlanTaskStatistic(long idPlan) {
        StatisticManager1 statisticManager = mdb.create(StatisticManager1)
        return statisticManager.getPlanTaskStatistic(idPlan)
    }


    StoreRecord loadGameServerRecInternal(long idGame, long idUsr) {
        StoreRecord recGame = mdb.createStoreRecord("Game.server")
        //
        mdb.loadQueryRecord(recGame, sqlGame(), [game: idGame, usr: idUsr])
        if (recGame.getLong("id") == 0) {
            return null
        }
        //
        return recGame
    }

    DataBox loadAndPrepareGame_Short(long idGame, long idUsr) {
        DataBox res = new DataBox()

        // Данные по игре
        StoreRecord recGame = loadGameServerRecInternal(idGame, idUsr)
        res.put("game", recGame)

        // Задания игры и результат их выполнения
        Store stGameTasksResult = mdb.createStore("GameTask.list")
        mdb.loadQuery(stGameTasksResult, sqlGameTasksResult(), [game: idGame, usr: idUsr])
        res.put("tasksResult", stGameTasksResult)

        //
        return res
    }

    DataBox loadAndPrepareGame(long idGame, long idUsr) {
        DataBox res = new DataBox()

        //
        StoreRecord recGame = loadGameServerRecInternal(idGame, idUsr)
        if (recGame == null) {
            return null
        }

        // Баллы (рейтинг) по текущей и прошлой игре
        StatisticManager1 statisticManager = mdb.create(StatisticManager1)
        Store stTasksStatistic = statisticManager.compareStatisticForGamePrior(idGame)
        //mdb.outTable(stTasksStatistic)

        // Общий рейтинг и проигранные баллы (плюсы и минусы)
        Map aggretate = statisticManager.aggregateStatistic(stTasksStatistic)
        aggretate.put("ratingMax", stTasksStatistic.size())

        // Задания игры и результат их выполнения +
        // Тело задания (вопрос и ответ) +
        // Подробный рейтинг
        Store stGameTasks = mdb.createStore("GameTask.list.statistic")
        mdb.loadQuery(stGameTasks, sqlGameTasksResult(), [game: idGame, usr: idUsr])

        // Задания в плане
        long idPlan = recGame.getLong("plan")
        Store stPlanTask = statisticManager.loadPlanTask(idPlan)

        //
        StoreUtils.join(stGameTasks, stPlanTask, "task", [
                "textQuestion",
                "soundQuestion",
                "textAnswer",
                "factQuestionDataType",
                "factQuestionValue",
                "factAnswerDataType",
                "factAnswerValue",
        ])
        StoreUtils.join(stGameTasks, stTasksStatistic, "task", [
                rating0  : "rating",
                ratingInc: "ratingInc",
                ratingDec: "ratingDec"
        ], false)

        //
        res.put("game", recGame)
        res.put("tasks", stGameTasks)
        //res.put("tasksStatistic", stTasksStatistic)
        res.put("statistic", aggretate)

        //
        return res
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
     * Преобразуем задание по требованиям frontend-api
     * Превращает списки stTaskQuestion и stTaskOption в пару плоских записей,
     * где имеем valueText, valueSound, valueImage и т.д.
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
        Store stTaskQuestion = task.get("taskQuestion")
        for (StoreRecord recTaskQuestion : stTaskQuestion) {
            convert(recTaskQuestion, resTask)
        }

        // Варианты ответа
        Store stTaskOption = task.get("taskOption")
        for (StoreRecord rec : stTaskOption) {
            StoreRecord recTaskOption = resTaskOption.add()
            recTaskOption.setValue("id", rec.getValue("id"))
            recTaskOption.setValue("isTrue", rec.getValue("isTrue"))
            recTaskOption.setValue("dataType", rec.getValue("dataType"))
            convert(rec, recTaskOption)
        }


        // ---
        DataBox res = new DataBox()
        res.put("task", resTask)
        res.put("taskOption", resTaskOption)


        //
        return res
    }


    void convert(StoreRecord recTaskSource, StoreRecord resTask) {
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


    private String sqlGameTaskOption() {
        return """
select
    TaskOption.*
from
    GameTask
    join Task on (GameTask.task = Task.id)
    join TaskOption on (Task.id = TaskOption.task)
where
    GameTask.id = :id and
    TaskOption.id = :taskOption
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

    private String sqlGameTasksResult() {
        return """
select
    GameTask.task,

    GameTask.dtTask,
    GameTask.dtAnswer,
    GameTask.wasTrue,
    GameTask.wasFalse,
    GameTask.wasHint,
    GameTask.wasSkip

from
    GameTask
    join Task on (GameTask.task = Task.id)

where
    GameTask.usr = :usr and
    GameTask.game = :game

order by
    GameTask.dtTask,
    GameTask.id
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

    private String sqlGame() {
        return """
select
    Game.*,
    Plan.text planText
from
    Game
    join Plan on (Game.plan = Plan.id)
    join GameUsr on (Game.id = GameUsr.game)
where
    Game.id = :game and
    GameUsr.usr = :usr
"""
    }

    private String sqlCloseActiveGame() {
        return """
update
    Game
set 
    dend = :dt
where
    Game.dend is null and
    Game.id in (
        select
            GameUsr.game 
        from 
            GameUsr
        where
            GameUsr.usr = :usr
    )
"""
    }


}
