package run.game.dao.backstage


import jandcode.core.dbm.std.*
import org.junit.jupiter.api.*
import run.game.dao.*

class Task_upd_Test extends RgmBase_Test {


    long idFact = 1001

    long idItem1 = 214
    long idItem2 = 1030

    long idTask = 1001


    @Test
    void loadTask() {
        Task_upd upd = mdb.create(Task_upd)

        //
        DataBox task = upd.loadTask(idTask)

        //
        println()
        printTask(task)
    }

    @Test
    void loadTasks() {
        Task_upd upd = mdb.create(Task_upd)

        //
        for (long idTask = 1000; idTask < 1010; idTask = idTask + 1) {
            try {
                DataBox task = upd.loadTask(idTask)

                //
                printTask(task)
            } catch (Exception e) {
                println(e.message)
            }
        }
    }


}
