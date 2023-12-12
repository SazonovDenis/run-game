package run.game.dao.game


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
        PlanCreator planCreator = mdb.create(PlanCreatorImpl)
        StoreRecord recPlan = planCreator.loadPlan(idPlan)


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


        // Добавляем задания на игру
        for (StoreRecord recTask : stTask) {
            mdb.insertRec("GameTask", [
                    game: idGame,
                    usr : idUsr,
                    task: recTask.getLong("task"),
            ])
        }


        //
        return loadGame(idGame)
    }


    @DaoMethod
    public DataBox choiceTask(long idGame) {
        // --- Выбираем, что осталось спросить по плану
        StoreRecord recGameTask = choiceGameTask(idGame)
        //
        if (recGameTask == null) {
            throw new XError("Все задания уже выполнены")
        }
        //
        long idTask = recGameTask.getLong("task")
        long idGameTask = recGameTask.getLong("id")


        // --- Грузим задание
        Task_upd upd = mdb.create(Task_upd)
        DataBox task = upd.loadTask(idTask)


        // --- Преобразуем задание по требованиям frontend-api
        DataBox res = prepareTask(task)


        // --- Записываем факт выдачи задания для пользователя
        XDateTime dt = XDateTime.now()
        mdb.updateRec("GameTask", [id: idGameTask, dtTask: dt])


        // --- Дополняем задание технической информацией
        StoreRecord resTask = res.get("task")
        resTask.setValue("id", idGameTask)
        resTask.setValue("dtTask", dt)

        //
        StoreRecord resGame = loadGame(idGame)
        res.put("game", resGame)


        //
        return res
    }


    @DaoMethod
    public void gameFinish(long idGame) {
        StoreRecord recGame = mdb.loadQueryRecord("select * from Game where id = :id", [id: idGame])

        XDateTime dt = XDateTime.now()
        recGame.setValue("dend", dt)

        mdb.updateRec("Game", recGame)
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


    DataBox prepareTask(DataBox task) {
        StoreRecord resTask = mdb.createStoreRecord("Task.Server")
        Store resTaskOption = mdb.createStore("TaskOption.Server")

        // Основной вопрос задания
        StoreRecord recTask = task.get("task")
        long dataTypeQuestion = recTask.getLong("dataTypeQuestion")
        long dataTypeAnswer = recTask.getLong("dataTypeAnswer")
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


    @DaoMethod
    Store getPlans() {
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)
        Store st = statisticManager.getPlanStatistic()

        //
        return st
    }


    StoreRecord choiceGameTask(long idGame) {
        long idUsr = getCurrentUserId()

        // Извлечем неотвеченные задания
        Store stGameTask = mdb.loadQuery(sqlSelectTask(), [
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


    String sqlSelectTask() {
        return """
select
    GameTask.*
from
    GameTask 
where
    game = :game and
    usr = :usr and
    -- неотвеченные
    dtAnswer is null 
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
