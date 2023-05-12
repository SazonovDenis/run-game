package run.game.dao.game


import jandcode.commons.datetime.*
import jandcode.core.dao.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.dao.backstage.*


public class ServerImpl extends RgMdbUtils implements Server {


    @DaoMethod
    public DataBox choiceTask(long idPaln) {
        StoreRecord resTask = mdb.createStoreRecord("Task.Server")
        Store resTaskOption = mdb.createStore("TaskOption.Server")

        // Выбираем, что спросить
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)
        // Выбираем факт и задание по этому факту
        long idFact = statisticManager.selectFact(idPaln)
        long idTask = statisticManager.selectTask(idFact)

        // Грузим задание
        TaskCreator taskCreator = mdb.create(TaskCreatorImpl)
        DataBox task = taskCreator.loadTask(idTask)

        // Основной вопрос задания
        StoreRecord recTask = task.get("task")
        resTask.setValue("dataType", recTask.getValue("dataType"))

        // Другие типы данных задания. Например, звук, если тип задания - текст
        Store stTaskQuestion = task.get("taskQuestion")
        for (StoreRecord recTaskQuestion : stTaskQuestion) {
            if (recTaskQuestion.getLong("dataType") == DbConst.DataType_word_sound) {
                resTask.setValue("sound", recTaskQuestion.getValue("value"))
            }
            if (recTaskQuestion.getLong("dataType") == DbConst.DataType_word_spelling) {
                resTask.setValue("text", recTaskQuestion.getValue("value"))
            }
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


        // Записываем факт выдачи задания для пользователя
        UsrTask_upd taskUpd = mdb.create(UsrTask_upd)
        StoreRecord recUsrTask = taskUpd.ins(recTask.getLong("id"))

        //
        resTask.setValue("id", recUsrTask.getValue("id"))
        resTask.setValue("taskDt", recUsrTask.getValue("taskDt"))


        // Выдаем задание
        DataBox res = new DataBox()
        res.put("task", resTask)
        res.put("taskOption", resTaskOption)


        //
        return res
    }


    @DaoMethod
    public void postTaskAnswer(long idUsrTask, long idTaskOption) {
        // Загрузим выданное задание. Если задание чужое, то запись
        // загрузится пустой и будет ошибка, что нормально.
        StoreRecord recUsrTask = mdb.loadQueryRecord(sqlUsrTask(), [id: idUsrTask, usr: getCurrentUserId()])
        // Выбранный вариант ответа. Если idTaskOption не соответствует idUsrTask,
        // запись загрузится пустой и будет ошибка, что нормально.
        StoreRecord recTaskOption = mdb.loadQueryRecord(sqlUsrTaskOption(), [id: idUsrTask, taskOption: idTaskOption])

        //
        if (!recUsrTask.isValueNull("answerTaskOption")) {
            mdb.validateErrors.addError("Ответ на задание уже дан")
        }
        //
        mdb.validateErrors.checkErrors()

        // Обновляем
        recUsrTask.setValue("answerTaskOption", recTaskOption.getValue("id"))
        recUsrTask.setValue("answerDt", XDateTime.now())
        mdb.updateRec("UsrTask", recUsrTask)
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


}
