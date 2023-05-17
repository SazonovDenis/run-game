package run.game.dao.game


import jandcode.commons.datetime.*
import jandcode.core.dao.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.dao.backstage.*


public class ServerImpl extends RgmMdbUtils implements Server {


    @DaoMethod
    public DataBox choiceTask(long idPaln) {
        StoreRecord resTask = mdb.createStoreRecord("Task.Server")
        Store resTaskOption = mdb.createStore("TaskOption.Server")

        // Выбираем, что спросить по плану
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)
        long idTask = statisticManager.selectTask(idPaln)

        // Грузим задание
        Task_upd upd = mdb.create(Task_upd)
        DataBox task = upd.loadTask(idTask)

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
    public void postTaskAnswer(long idUsrTask, Map taskResult) {
        // Загрузим выданное задание. Если задание чужое, то запись
        // загрузится пустой и будет ошибка, что нормально.
        StoreRecord recUsrTask = mdb.loadQueryRecord(sqlUsrTask(), [id: idUsrTask, usr: getCurrentUserId()])


        // Валидация
        if (taskResult.get("wasTrue") == false &&
                taskResult.get("wasFalse") == false &&
                taskResult.get("wasHint") == false &&
                taskResult.get("wasSkip") == false
        ) {
            mdb.validateErrors.addError("Не указано состояние ответа на задание")
        }

        //
        if (!recUsrTask.isValueNull("answerDt")) {
            mdb.validateErrors.addError("Ответ на задание уже дан")
        }

        //
        mdb.validateErrors.checkErrors()


        // Обновляем
        recUsrTask.setValue("answerDt", XDateTime.now())
        recUsrTask.setValue("wasTrue", taskResult.get("wasTrue"))
        recUsrTask.setValue("wasFalse", taskResult.get("wasFalse"))
        recUsrTask.setValue("wasHint", taskResult.get("wasHint"))
        recUsrTask.setValue("wasSkip", taskResult.get("wasSkip"))
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
