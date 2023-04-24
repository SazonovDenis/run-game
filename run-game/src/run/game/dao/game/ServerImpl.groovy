package run.game.dao.game

import jandcode.commons.datetime.*
import jandcode.core.dao.*
import jandcode.core.dbm.mdb.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.backstage.*

/**
 *
 */
public class ServerImpl extends BaseMdbUtils implements Server {


    @DaoMethod
    public DataBox choiceTask(long idPaln) {
        // Выбираем факт
        long idFact = selectFact(idPaln)

        // Выбираем задание
        long idTask = selectTask(idFact)


        // Грузим задание
        TaskCreator taskCreator = mdb.create(TaskCreatorImpl)
        DataBox task = taskCreator.loadTask(idTask)

        // Не показываем правильный ответ на задание
        StoreRecord recTask = task.get("task")
        recTask.setValue("factAnswer", null)
        //
        Store stTaskOption = task.get("taskOption")
        for (StoreRecord recTaskOption : stTaskOption) {
            recTaskOption.setValue("trueFact", null)
        }


        // Формируем задание для пользователя
        StoreRecord recUsrTask = mdb.createStoreRecord("UsrTask")
        //recUsrTask.setValue("usr", idUsr)
        recUsrTask.setValue("task", recTask.getValue("id"))
        recUsrTask.setValue("taskDt", XDateTime.now())

        //
        long idUsrTask = mdb.insertRec("UsrTask", recUsrTask)
        recUsrTask.setValue("id", idUsrTask)


        // Выдаем задание для пользователя
        DataBox res = new DataBox()
        res.put("task", recTask)
        res.put("taskOption", stTaskOption)
        res.put("usrTask", recUsrTask)


        //
        return res;
    }


    @DaoMethod
    public void postTaskAnswer(long idUsrTask, long idTaskOption) {
        // Задание для пользователя
        StoreRecord recUsrTask = mdb.loadQueryRecord(sqlUsrTask(), [id: idUsrTask])
        // Выбранный вариант ответа.
        // Если idTaskOption не соответствует idUsrTask, запись загрузится пустой
        StoreRecord recTaskOption = mdb.loadQueryRecord(sqlUsrTaskOption(), [id: idUsrTask, taskOption: idTaskOption])

        // Обновляем
        recUsrTask.setValue("answerTaskOption", recTaskOption.getValue("id"))
        recUsrTask.setValue("answerDt", XDateTime.now())
        mdb.updateRec("UsrTask", recUsrTask)
    }


    @DaoMethod
    public Store getFactStatistic(long idFact) {
        return null;
    }


    @DaoMethod
    public Map<Long, Store> getPlanStatistic(long idPaln) {
        return null;
    }


    String sqlUsrTask() {
        return """
select * 
from UsrTask
where UsrTask.id = :id 
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


    /**
     * Выбирает подходящий факт
     */
    long selectFact(long idPlan) {
        Store stFact = mdb.loadQuery("select id from Fact", [])
        long idFact = stFact.get(0).getLong("id")
        return idFact
    }

    /**
     * Выбирает подходящее задание
     */
    long selectTask(long idFact) {
        //Store stTask = mdb.loadQuery("select id from Task where factQuestion = :fact", [fact: idFact])
        Store stTask = mdb.loadQuery("select id from Task limit 10", [])
        long idTask = stTask.get(0).getLong("id")
        return idTask
    }
}
