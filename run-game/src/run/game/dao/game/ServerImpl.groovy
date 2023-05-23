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
    public long gameStart(long idPlan) {
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
        Store stTask = statisticManager.getUsrStatisticByPlan(idUsr, idPlan)


        // Добавляем задания на игру
        for (StoreRecord recTask : stTask) {
            mdb.insertRec("UsrTask", [
                    game: idGame,
                    usr : idUsr,
                    task: recTask.getLong("task"),
            ])
        }


        //
        return idGame
    }


    @DaoMethod
    public DataBox choiceTask(long idGame) {
        // --- Выбираем, что осталось спросить по плану
        StoreRecord recUsrTask = choiceUsrTask(idGame)
        //
        if (recUsrTask == null) {
            throw new XError("Все задания уже выполнены")
        }
        //
        long idTask = recUsrTask.getLong("task")
        long idUsrTask = recUsrTask.getLong("id")


        // --- Грузим задание
        Task_upd upd = mdb.create(Task_upd)
        DataBox task = upd.loadTask(idTask)


        // --- Преобразуем задание по требованиям frontend-api
        DataBox res = prepareTask(task)


        // --- Записываем факт выдачи задания для пользователя
        UsrTask_upd taskUpd = mdb.create(UsrTask_upd)
        XDateTime dt = XDateTime.now()
        taskUpd.markDt(idUsrTask, dt)


        // --- Дополняем задание технической информацией
        StoreRecord resTask = res.get("task")
        resTask.setValue("id", idUsrTask)
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


    @DaoMethod
    public void postTaskAnswer(long idUsrTask, Map taskResult) {
        // Загрузим выданное задание. Если задание чужое, то запись
        // загрузится пустой и будет ошибка, что нормально.
        long idUsr = getCurrentUserId()
        StoreRecord recUsrTask = mdb.loadQueryRecord(sqlUsrTask(), [id: idUsrTask, usr: idUsr])


        // Валидация
        if (taskResult.get("wasTrue") == false &&
                taskResult.get("wasFalse") == false &&
                taskResult.get("wasHint") == false &&
                taskResult.get("wasSkip") == false
        ) {
            mdb.validateErrors.addError("Не указано состояние ответа на задание")
        }

        //
        if (!recUsrTask.isValueNull("dtAnswer")) {
            mdb.validateErrors.addError("Ответ на задание уже дан")
        }

        //
        mdb.validateErrors.checkErrors()


        // Обновляем
        recUsrTask.setValue("dtAnswer", XDateTime.now())
        recUsrTask.setValue("wasTrue", taskResult.get("wasTrue"))
        recUsrTask.setValue("wasFalse", taskResult.get("wasFalse"))
        recUsrTask.setValue("wasHint", taskResult.get("wasHint"))
        recUsrTask.setValue("wasSkip", taskResult.get("wasSkip"))
        mdb.updateRec("UsrTask", recUsrTask)
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
        //^c Загрузка и выбор планов

        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)
        Store st = statisticManager.getUsrStatistic(getCurrentUserId())
        return st
    }


    StoreRecord choiceUsrTask(long idGame) {
        long idUsr = getCurrentUserId()

        // Извлечем неотвеченные задания
        Store stUsrTask = mdb.loadQuery(sqlSelectTask(), [
                game: idGame,
                usr : idUsr,
        ])

        if (stUsrTask.size() == 0) {
            return null
        }

        // Выберем случайное
        int n = rnd.num(0, stUsrTask.size() - 1)
        StoreRecord rec = stUsrTask.get(n)

        //
        return rec
    }


    String sqlSelectTask() {
        return """
select
    UsrTask.*
from
    UsrTask 
where
    game = :game and
    usr = :usr and
    -- неотвеченные
    dtAnswer is null 
"""
    }


    String sqlUsrTask() {
        return """
select 
    * 
from 
    UsrTask
where
    UsrTask.id = :id and 
    UsrTask.usr = :usr 
"""
    }


    String sqlUsrTaskOption() {
        return """
select
    TaskOption.*
from
    UsrTask
    join Task on (UsrTask.task = Task.id)
    join TaskOption on (Task.id = TaskOption.task)
where
    UsrTask.id = :id and
    TaskOption.id = :taskOption
"""
    }

    String sqlGame() {
        return """
select
    Plan.text,
    Game.id,
    count(*) as countTotal,
    sum(case when UsrTask.dtTask is null then 0 else 1 end) as countDone
from
    Game
    join Plan on (Game.plan = Plan.id)
    join UsrTask on (Game.id = UsrTask.game)
where
    Game.id = :game
group by    
    Plan.text,
    Game.id
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
}
