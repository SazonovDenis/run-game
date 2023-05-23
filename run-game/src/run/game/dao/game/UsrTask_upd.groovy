package run.game.dao.game

import jandcode.commons.datetime.*
import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*

class UsrTask_upd extends RgmMdbUtils {

    /**
     * Записывает, что задание idTask, было выдано текущему пользователю
     * @param task Task.id
     * @return UsrTask.id
     */
    @DaoMethod
    StoreRecord ins(long idGame, long idTask) {
        long idUsr = getCurrentUserId()
        XDateTime dt = XDateTime.now()

        //
        StoreRecord recUsrTask = mdb.createStoreRecord("UsrTask")
        recUsrTask.setValue("task", idGame)
        recUsrTask.setValue("usr", idUsr)
        recUsrTask.setValue("task", idTask)
        recUsrTask.setValue("dtTask", dt)

        //
        long idUsrTask = mdb.insertRec("UsrTask", recUsrTask)
        recUsrTask.setValue("id", idUsrTask)

        //
        return recUsrTask
    }

    void markDt(long idUsrTask, XDateTime dt) {
        mdb.updateRec("UsrTask", [id: idUsrTask, dtTask: dt])
    }

}
