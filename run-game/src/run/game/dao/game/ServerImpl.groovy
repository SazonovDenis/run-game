package run.game.dao.game

import jandcode.commons.*
import jandcode.commons.datetime.*
import jandcode.commons.error.*
import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.dao.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.dao.backstage.*

public class ServerImpl extends RgmMdbUtils implements Server {

    long MAX_TASK_FOR_GAME = 10

    Rnd rnd = new RndImpl()


    @DaoMethod
    public StoreRecord getActiveGame() {
        long idUsr = getCurrentUserId()
        Store storeGames = mdb.loadQuery(sqlActiveGame(), [usr: idUsr])

        if (storeGames.size() != 0) {
            long idGame = storeGames.get(0).getLong("id")
            return loadGame(idGame)
        } else {
            return null
        }
    }


    @DaoMethod
    public void closeActiveGame() {
        long idUsr = getCurrentUserId()
        XDateTime dt = XDateTime.now()
        mdb.execQuery(sqlCloseActiveGame(), [usr: idUsr, dt: dt])
    }


    @DaoMethod
    public StoreRecord gameStart(long idPlan) {
        // Добавляем Game
        StoreRecord recGame = mdb.createStoreRecord("Game")
        //
        XDateTime dt = XDateTime.now()
        recGame.setValue("dbeg", dt)
        //
        recGame.setValue("plan", idPlan)

        //
        long idGame = mdb.insertRec("Game", recGame)


        // Добавим участника игры
        long idUsr = getCurrentUserId()
        mdb.insertRec("GameUsr", [usr: idUsr, game: idGame])


        // Отберем подходящие задания на игру.
        // Задание выбирается с учетом статистики пользователя.
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)
        Store stTask = statisticManager.getTaskStatisticByPlan(idPlan)
        //stTask.sort("progress")
        //mdb.outTable(stTask, 20)

        // Слегка рандомизируем рейтинг -
        // иначе для для заданий без рейтинга (например, которые никогда не выдавали)
        // рейтинг перестает быть хорошим выбором порядка - получается список идущих подряд
        // одних тех же слов (потому, что задания сортируются по номеру факта,
        // а для одного факта несколько заданий)
        for (StoreRecord recTask : stTask) {
            double progressSeed = rnd.num(-1000, 1000) / 10000
            recTask.setValue("progressprogress", recTask.getDouble("progress") + progressSeed)
        }

        // Теперь выберем задания на игру по рейтингу
        stTask.sort("progress")
        //mdb.outTable(stTask, 20)
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

            //
            if (taskForGameCount >= MAX_TASK_FOR_GAME) {
                break
            }
        }


        //
        return loadGame(idGame)
    }


    @DaoMethod
    public DataBox choiceTask(long idGame) {
        // Выбираем, что осталось спросить по плану
        StoreRecord recGameTask = choiceGameTask(idGame)

        //
        if (recGameTask == null) {
            throw new XError("Все задания уже выполнены")
        }

        //
        return loadAndPrepareTask(recGameTask)
    }


    @DaoMethod
    public DataBox currentTask(long idGame) {
        // Выбираем, последнее выданное задание
        StoreRecord recGameTask = getGameLastTask(idGame)

        //
        if (recGameTask == null) {
            throw new XError("Все задания уже выполнены")
        }

        //
        return loadAndPrepareTask(recGameTask)
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


        // Обновляем задание
        recGameTask.setValue("dtAnswer", XDateTime.now())
        recGameTask.setValue("wasTrue", taskResult.get("wasTrue"))
        recGameTask.setValue("wasFalse", taskResult.get("wasFalse"))
        recGameTask.setValue("wasHint", taskResult.get("wasHint"))
        recGameTask.setValue("wasSkip", taskResult.get("wasSkip"))
        mdb.updateRec("GameTask", recGameTask)
    }


    /**
     * Список уровней, со статистикой по словам,
     * сортированный по некоторому критерию, например по самому отстающему
     * или по запланированному учителем.
     */
    @DaoMethod
    Store getPlans() {
        Store res = mdb.createStore("Plan.list.statistic")

        //
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)
        Store st = statisticManager.getPlanStatistic()

        //
        for (StoreRecord rec : st) {
            StoreRecord recRes = res.add()
            recRes.setValue("id", rec.getValue("id"))
            recRes.setValue("text", rec.getValue("text"))
            recRes.setValue("count", rec.getValue("count"))
            recRes.setValue("progress", rec.getValue("progress"))
            recRes.setValue("taskInfo", UtJson.fromJson(rec.getString("taskInfo")))
        }

        //
        //res.sort("*progress")

        //
        return res
    }

    public DataBox loadAndPrepareTask(StoreRecord recGameTask) {
        //
        long idTask = recGameTask.getLong("task")
        long idGameTask = recGameTask.getLong("id")
        long idGame = recGameTask.getLong("game")


        // --- Грузим задание
        Task_upd upd = mdb.create(Task_upd)
        DataBox task = upd.loadTask(idTask)


        // --- Преобразуем задание по требованиям frontend-api
        DataBox res = prepareTask(task)


        // --- Записываем факт выдачи задания для пользователя
        XDateTime dtTask = recGameTask.getDateTime("dtTask")
        if (UtDateTime.isEmpty(dtTask)) {
            dtTask = XDateTime.now()
            mdb.updateRec("GameTask", [id: idGameTask, dtTask: dtTask])
        }


        // --- Дополняем задание технической информацией
        StoreRecord resTask = res.get("task")
        resTask.setValue("id", idGameTask)
        resTask.setValue("dtTask", dtTask)

        //
        StoreRecord resGame = loadGame(idGame)
        res.put("game", resGame)


        //
        return res
    }

    DataBox prepareTask(DataBox task) {
        StoreRecord resTask = mdb.createStoreRecord("Task.Server")
        Store resTaskOption = mdb.createStore("TaskOption.Server")

        // Основной вопрос задания
        StoreRecord recTask = task.get("task")
        long dataTypeQuestion = recTask.getLong("dataTypeQuestion")
        long dataTypeAnswer = recTask.getLong("dataTypeAnswer")
        resTask.setValue("task", recTask.getLong("id"))
        resTask.setValue("dataTypeQuestion", dataTypeQuestion)
        resTask.setValue("dataTypeAnswer", dataTypeAnswer)

        // Заполняем данные задания по типам.
        Store stTaskQuestion = task.get("taskQuestion")
        String valueSound = null
        String valueTranslate = null
        String valueSpelling = null
        for (StoreRecord recTaskQuestion : stTaskQuestion) {
            if (recTaskQuestion.getLong("dataType") == RgmDbConst.DataType_word_sound) {
                valueSound = recTaskQuestion.getValue("value")
            }
            // Текст
            if (recTaskQuestion.getLong("dataType") == RgmDbConst.DataType_word_spelling) {
                valueSpelling = recTaskQuestion.getValue("value")
            }
            if (recTaskQuestion.getLong("dataType") == RgmDbConst.DataType_word_translate) {
                valueTranslate = recTaskQuestion.getValue("value")
            }
        }
        // Звук попадает всегда
        resTask.setValue("sound", valueSound)
        // Текст зависит от вопроса
        if (dataTypeQuestion == RgmDbConst.DataType_word_spelling || dataTypeQuestion == RgmDbConst.DataType_word_sound) {
            resTask.setValue("text", valueSpelling)
        } else {
            resTask.setValue("text", valueTranslate)
        }

        // Варианты ответа
        Store stTaskOption = task.get("taskOption")
        for (StoreRecord recTaskOption : stTaskOption) {
            resTaskOption.add([
                    "id"    : recTaskOption.getValue("id"),
                    "text"  : recTaskOption.getValue("value"),
                    "isTrue": recTaskOption.getValue("isTrue"),
            ])
        }


        //
        DataBox res = new DataBox()
        res.put("task", resTask)
        res.put("taskOption", resTaskOption)


        //
        return res
    }


    StoreRecord choiceGameTask(long idGame) {
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


    StoreRecord getGameLastTask(long idGame) {
        long idUsr = getCurrentUserId()

        // Извлечем выданное, но не отвеченное задание
        Store stGameTask = mdb.loadQuery(sqlGetTask(), [
                game: idGame,
                usr : idUsr,
        ])

        // Выберем последнее
        StoreRecord rec = stGameTask.get(0)

        //
        return rec
    }


    String sqlChoiceTask() {
        return """
select
    GameTask.*
from
    GameTask 
where
    game = :game and
    usr = :usr and
    -- неотвеченные
    dtTask is null 
"""
    }


    String sqlGetTask() {
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


    String sqlGameTask() {
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


    String sqlGameTaskOption() {
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

    StoreRecord loadGame(long idGame) {
        StoreRecord resGame = mdb.createStoreRecord("Game.Server")

        //
        StoreRecord recGame = mdb.loadQueryRecord(sqlGame(), [game: idGame])
        resGame.setValues(recGame)

        //
        return resGame
    }


    String sqlActiveGame() {
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

    String sqlCloseActiveGame() {
        return """
update
    Game
set 
    dend = :dt
where
    Game.id in (
        select GameUsr.game from GameUsr where GameUsr.usr = :usr
    )
"""
    }

    String sqlGame() {
        return """
select
    Game.id,
    Game.plan,
    Game.dbeg,
    Game.dend,
    Plan.text,
    count(*) as countTotal,
    sum(case when GameTask.dtTask is null then 0 else 1 end) as countDone
from
    Game
    join Plan on (Game.plan = Plan.id)
    join GameTask on (Game.id = GameTask.game)
where
    Game.id = :game
group by    
    Plan.text,
    Game.id
"""
    }


}
