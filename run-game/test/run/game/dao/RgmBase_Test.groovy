package run.game.dao

import jandcode.commons.*
import jandcode.commons.error.*
import jandcode.core.apx.test.*
import jandcode.core.auth.*
import jandcode.core.auth.std.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*

class RgmBase_Test extends Apx_Test {

    AuthService authSvc

    RngStopWatch sw = new RngStopWatch()

    void setUp() throws Exception {
        super.setUp()

        //
        authSvc = app.bean(AuthService.class)

        //
        setCurrentUser(new DefaultUserPasswdAuthToken("admin", "111"))
        println(authSvc.getCurrentUser().attrs)
    }

    void setCurrentUser(UserPasswdAuthToken authToken) {
        AuthUser user = authSvc.login(authToken)
        authSvc.setCurrentUser(user)
    }

    long getCurrentUserId() {
        AuthUser user = authSvc.getCurrentUser()
        long id = UtCnv.toLong(user.getAttrs().getLong("id"))
        if (id == 0) {
            throw new XError("Пользователь не указан")
        }
        return id
    }

    void printTasks(Collection<DataBox> tasks) {
        for (DataBox task : tasks) {
            println()
            printTask(task)
        }
    }

    void printTasksOneLine(Collection<DataBox> tasks) {
        for (DataBox task : tasks) {
            printTaskOneLine(task)
        }
    }

    void printTask(DataBox task) {
        mdb.resolveDicts(task)
        //
        println("task")
        utils.outTable(task.get("task"))
        //
        println("taskQuestion")
        if (task.containsKey("taskQuestion")) {
            utils.outTable(task.get("taskQuestion"))
        } else {
            println("<null>")
        }
        //
        println("taskOption")
        utils.outTable(task.get("taskOption"))
        //
        println("game")
        if (task.containsKey("game")) {
            utils.outTable(task.get("game"))
        } else {
            println("<null>")
        }
    }

    void printTaskOneLine(DataBox task) {
        mdb.resolveDicts(task)

        if (!task.containsKey("taskQuestion")) {
            printTaskOneLine_choiced(task)
        } else {
            if (task.get("task").findField("dataTypeQuestion")) {
                printTaskOneLine_loaded(task)
            } else {
                printTaskOneLine_created(task)
            }
        }
    }

    void printFact(StoreRecord rec) {
        mdb.resolveDicts(rec)
        utils.outTable(rec)
    }

    void printFacts(Store st) {
        mdb.resolveDicts(st)
        utils.outTable(st)
    }

    void printTaskOneLine_loaded(DataBox task) {
        String strTaskDataType = task.get("task").getValue("dataTypeQuestion") + " -> " + task.get("task").getValue("dataTypeAnswer")
        String strTaskQuestion = task.get("taskQuestion").getUniqueValues("value").join(" | ")
        String strTaskOption = task.get("taskOption").getUniqueValues("value").join(" | ")
        println(strTaskDataType + ", Q: " + strTaskQuestion + ", A: " + strTaskOption)
    }

    void printTaskOneLine_choiced(DataBox task) {
        String strTaskDataType = task.get("task").getValue("dataTypeQuestion") + " -> " + task.get("task").getValue("dataTypeAnswer")
        String strTaskQuestion = task.get("task").getValue("text") + " | " + task.get("task").getValue("sound")
        String strTaskOption = task.get("taskOption").getUniqueValues("text").join(" | ")
        println(strTaskDataType + ", Q: " + strTaskQuestion + ", A: " + strTaskOption)
    }

    void printTaskOneLine_created(DataBox task) {
        String strTaskDataType = task.get("taskQuestion").get(0).getValue("dataType") + " -> " + task.get("taskOption").get(0).getValue("dataType")
        String strTaskQuestion = task.get("taskQuestion").getUniqueValues("value").join(" | ")
        String strTaskOption = task.get("taskOption").getUniqueValues("value").join(" | ")
        println(strTaskDataType + ", Q: " + strTaskQuestion + ", A: " + strTaskOption)
    }


}
