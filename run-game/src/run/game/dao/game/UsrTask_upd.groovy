package run.game.dao.game

import jandcode.commons.datetime.*
import jandcode.core.dao.*
import jandcode.core.dbm.mdb.*
import jandcode.core.store.*

class UsrTask_upd extends BaseMdbUtils {

    long getUsr() {
        return 1010
    }

    @DaoMethod
    StoreRecord ins(long task) {
        StoreRecord recUsrTask = mdb.createStoreRecord("UsrTask")

        //
        recUsrTask.setValue("usr", getUsr())
        recUsrTask.setValue("task", task)
        recUsrTask.setValue("taskDt", XDateTime.now())
        //
        long idUsrTask = mdb.insertRec("UsrTask", recUsrTask)
        recUsrTask.setValue("id", idUsrTask)

        //
        return recUsrTask
    }

}
