package run.game.dao.game

import jandcode.commons.datetime.*
import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*

class UsrTask_upd extends RgMdbUtils {

    /**
     * Записывает, что задание task, было выдано текущему пользователю
     * @param task Task.id
     * @return UsrTask.id
     */
    @DaoMethod
    StoreRecord ins(long task) {
        StoreRecord recUsrTask = mdb.createStoreRecord("UsrTask")

        //
        recUsrTask.setValue("usr", getCurrentUserId())
        recUsrTask.setValue("task", task)
        recUsrTask.setValue("taskDt", XDateTime.now())
        //
        long idUsrTask = mdb.insertRec("UsrTask", recUsrTask)
        recUsrTask.setValue("id", idUsrTask)

        //
        return recUsrTask
    }

}
