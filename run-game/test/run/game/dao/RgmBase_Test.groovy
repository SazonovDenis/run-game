package run.game.dao

import jandcode.commons.*
import jandcode.commons.error.*
import jandcode.commons.rnd.*
import jandcode.core.apx.test.*
import jandcode.core.auth.*
import jandcode.core.auth.std.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*

class RgmBase_Test extends Apx_Test {

    AuthService authSvc

    RngStopWatch sw = new RngStopWatch()

    Rnd rnd = Rnd.create(123456789)

    void setUp() throws Exception {
        super.setUp()

        //
        authSvc = app.bean(AuthService.class)

        //
        setCurrentUser("admin", "111")
    }

    void setCurrentUser(String login, String password) {
        setCurrentUser(new DefaultUserPasswdAuthToken(login, password))
        println("CurrentUser: " + authSvc.getCurrentUser().attrs.get("login") + ":" + authSvc.getCurrentUser().attrs.get("id") + ", attrs: " + authSvc.getCurrentUser().attrs)
    }

    void setCurrentUser(UserPasswdAuthToken authToken) {
        AuthUser user = authSvc.login(authToken)
        authSvc.setCurrentUser(user)
    }

    void setCurrentUserId(long idUsr) {
        StoreRecord rec = mdb.loadQueryRecord("select * from Usr where id = " + idUsr)
        setCurrentUser(rec.getString("login"), rec.getString("password"))
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
    }

    void printTaskOneLine(DataBox task) {
        mdb.resolveDicts(task)

        if (!task.containsKey("taskQuestion")) {
            printTaskOneLine_choiced(task)
        } else {
            if (task.get("task").findField("factTypeQuestion")) {
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
        printFacts(st, Integer.MAX_VALUE)
    }

    void printFacts(Store st, int limit) {
        mdb.resolveDicts(st)
        utils.outTable(st, limit)
    }

    void printTaskOneLine_loaded(DataBox task) {
        String strTaskFactType = task.get("task").getValue("factTypeQuestion") + " -> " + task.get("task").getValue("factTypeAnswer")
        String strTaskQuestion = task.get("taskQuestion").getUniqueValues("value").join(" | ")
        String strTaskOption = task.get("taskOption").getUniqueValues("value").join(" | ")
        println(strTaskFactType + ", Q: " + strTaskQuestion + ", A: " + strTaskOption)
    }

    void printTaskOneLine_choiced(DataBox task) {
        String strTaskFactType = task.get("task").getValue("factTypeQuestion") + " -> " + task.get("task").getValue("factTypeAnswer")
        String strTaskQuestion = task.get("task").getValue("text") + " | " + task.get("task").getValue("sound")
        String strTaskOption = task.get("taskOption").getUniqueValues("text").join(" | ")
        println(strTaskFactType + ", Q: " + strTaskQuestion + ", A: " + strTaskOption)
    }

    void printTaskOneLine_created(DataBox task) {
        String strTaskFactType = task.get("taskQuestion").get(0).getValue("factType") + " -> " + task.get("taskOption").get(0).getValue("factType")
        String strTaskQuestion = task.get("taskQuestion").getUniqueValues("value").join(" | ")
        String strTaskOption = task.get("taskOption").getUniqueValues("value").join(" | ")
        println(strTaskFactType + ", Q: " + strTaskQuestion + ", A: " + strTaskOption)
    }


}
