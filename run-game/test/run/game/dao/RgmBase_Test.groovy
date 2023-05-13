package run.game.dao

import jandcode.core.apx.test.*
import jandcode.core.auth.*
import jandcode.core.auth.std.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*

class RgmBase_Test extends Apx_Test {

    AuthService authSvc

    void setUp() throws Exception {
        super.setUp()

        authSvc = app.bean(AuthService.class)
        AuthUser user = authSvc.login(new DefaultUserPasswdAuthToken("admin", "111"))
        authSvc.setCurrentUser(user)
        println(authSvc.getCurrentUser().attrs)
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
        println("task")
        utils.outTable(task.get("task"))
        println("taskQuestion")
        if (task.containsKey("taskQuestion")) {
            utils.outTable(task.get("taskQuestion"))
        } else {
            println("<null>")
        }
        println("taskOption")
        utils.outTable(task.get("taskOption"))
    }

    void printTaskOneLine(DataBox task) {
        mdb.resolveDicts(task)
        String strDataTypeQuestion = ""
        if (task.get("task").findField("dataTypeQuestion") != null) {
            strDataTypeQuestion = ", " + task.get("task").getValue("dataTypeQuestion")
        }
        println(task.get("task").getValue("id") + strDataTypeQuestion + ", question: " + task.get("taskQuestion").getUniqueValues("value").join(" | ") + ", option: " + task.get("taskOption").getUniqueValues("value").join(" | "))
    }

    void printFact(StoreRecord rec) {
        mdb.resolveDicts(rec)
        utils.outTable(rec)
    }

    void printFacts(Store st) {
        mdb.resolveDicts(st)
        utils.outTable(st)
    }


}
