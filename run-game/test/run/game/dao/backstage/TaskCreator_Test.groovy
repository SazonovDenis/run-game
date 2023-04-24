package run.game.dao.backstage


import jandcode.core.apx.test.*
import jandcode.core.dbm.std.*
import org.junit.jupiter.api.*

class TaskCreator_Test extends Apx_Test {


    @Test
    void loadTask() {
        TaskCreator taskCreator = mdb.create(TaskCreatorImpl)

        //
        long idTask = 1002
        DataBox task = taskCreator.loadTask(idTask)

        //
        println()
        printTask(task)
    }

    @Test
    void loadTasks() {
        TaskCreator taskCreator = mdb.create(TaskCreatorImpl)

        //
        for (long idTask = 1001; idTask < 1200; idTask = idTask + 3) {
            try {
                DataBox task = taskCreator.loadTask(idTask)

                //
                printTaskOneLine(task)
            } catch (Exception e) {
                println(e.message)
            }
        }
    }

    @Test
    void createTask1() {
        String word = "different"
        long idItem = mdb.loadQueryRecord("select * from Item where Item.value = :value", [value: word]).getLong("id")

        //
        TaskCreator taskCreator = mdb.create(TaskCreatorImpl)

        //
        for (int i = 0; i < 10; i++) {
            DataBox task = taskCreator.createTask(idItem, "word-spelling", "word-translate")

            //
            println()
            printTask(task)
        }
    }

    @Test
    void createTask2() {
        String word = "break in"
        long idItem = mdb.loadQueryRecord("select * from Item where Item.value = :value", [value: word]).getLong("id")

        //
        TaskCreator taskCreator = mdb.create(TaskCreatorImpl)

        //
        for (int i = 0; i < 5; i++) {
            DataBox task = taskCreator.createTask(idItem, "word-spelling", "word-translate")

            //
            println()
            printTask(task)
        }
    }

    @Test
    void saveTask() {
        String word = "simple"
        long idItem = mdb.loadQueryRecord("select * from Item where Item.value = :value", [value: word]).getLong("id")

        //
        TaskCreator taskCreator = mdb.create(TaskCreatorImpl)

        //
        DataBox task = taskCreator.createTask(idItem, "word-spelling", "word-translate")

        //
        println()
        printTask(task)

        //
        taskCreator.saveTask(task)
    }

    @Test
    void saveTasks() {
        TaskCreator taskCreator = mdb.create(TaskCreatorImpl)

        //
        long idItem = 1001
        for (int j = 0; j < 20; j++) {
            for (int i = 0; i < 5; i++) {
                try {
                    //
                    DataBox task = taskCreator.createTask(idItem, "word-spelling", "word-translate")

                    //
                    taskCreator.saveTask(task)

                    //
                    printTaskOneLine(task)
                } catch (Exception e) {
                    println(e.message)
                }
            }
            //
            idItem = idItem + 7
        }
    }

    void printTask(DataBox task) {
        mdb.resolveDicts(task)
        utils.outTable(task.get("task"))
        utils.outTable(task.get("taskOption"))
    }

    void printTaskOneLine(DataBox task) {
        println(task.get("task").getValue("value") + ": " + task.get("taskOption").getUniqueValues("value").join(" | "))
    }

}
