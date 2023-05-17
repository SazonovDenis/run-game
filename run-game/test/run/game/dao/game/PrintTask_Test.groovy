package run.game.dao.game

import jandcode.core.dbm.std.*
import org.junit.jupiter.api.*
import run.game.dao.*
import run.game.dao.backstage.*

class PrintTask_Test extends RgmBase_Test {


    @Test
    void print() {
        Server upd1 = mdb.create(ServerImpl)
        DataBox task1 = upd1.choiceTask(9999)
        println()
        println("---")
        printTask(task1)
        println()
        printTaskOneLine(task1)

        long idTask = 1001
        Task_upd upd2 = mdb.create(Task_upd)
        DataBox task2 = upd2.loadTask(idTask)
        println()
        println("---")
        printTask(task2)
        println()
        printTaskOneLine(task2)

        long idItem = 1001
        TaskGenerator taskCreator = mdb.create(TaskGeneratorImpl)
        Collection<DataBox> tasks = taskCreator.createTasks(idItem, RgmDbConst.DataType_word_spelling, RgmDbConst.DataType_word_translate, 1)
        println()
        println("---")
        printTasks(tasks)
        println()
        printTasksOneLine(tasks)
    }

}
