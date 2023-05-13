package run.game.dao

import jandcode.core.apx.test.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*

class RgmBase_Test extends Apx_Test {

    void printTasks(Collection<DataBox> tasks) {
        for (DataBox task : tasks) {
            println()
            printTask(task)
        }
    }

    void printTask(DataBox task) {
        mdb.resolveDicts(task)
        println("task")
        utils.outTable(task.get("task"))
        println("taskQuestion")
        utils.outTable(task.get("taskQuestion"))
        println("taskOption")
        utils.outTable(task.get("taskOption"))
    }

    void printTaskOneLine(DataBox task) {
        mdb.resolveDicts(task)
        println(task.get("task").getValue("id") + ", " + task.get("task").getValue("value") + ": " + task.get("taskOption").getUniqueValues("value").join(" | "))
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
