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
        // Выбираем
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)
        // Выбираем факт
        long idFact = statisticManager.selectFact(idPaln)
        // Выбираем задание
        long idTask = statisticManager.selectTask(idFact)


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


}
