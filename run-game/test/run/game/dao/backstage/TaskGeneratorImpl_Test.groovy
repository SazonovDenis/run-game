package run.game.dao.backstage


import jandcode.core.dbm.std.*
import org.junit.jupiter.api.*
import run.game.dao.*

class TaskGeneratorImpl_Test extends RgmBase_Test {

    long idItem2 = 1030

    @Test
    void createTask_forItem_rus_eng() {
        TaskGeneratorImpl taskCreator = mdb.create(TaskGeneratorImpl)

        //
        Collection<DataBox> tasks2 = taskCreator.createTasks(idItem2, "word-translate", "word-spelling")

        //
        println()
        println("word-translate -> word-spelling")
        printTasks(tasks2)

        //
        Task_upd upd = mdb.create(Task_upd)
        for (DataBox task : tasks2) {
            upd.saveTask(task)
        }
    }

    @Test
    void createTask_forItem() {
        TaskGeneratorImpl taskCreator = mdb.create(TaskGeneratorImpl)

        //
        Collection<DataBox> tasks1 = taskCreator.createTasks(idItem2, "word-spelling", "word-translate")
        Collection<DataBox> tasks2 = taskCreator.createTasks(idItem2, "word-translate", "word-spelling")
        Collection<DataBox> tasks3 = taskCreator.createTasks(idItem2, "word-sound", "word-translate")
        Collection<DataBox> tasks4 = taskCreator.createTasks(idItem2, "word-sound", "word-spelling")

        //
        println()
        println("word-spelling -> word-translate")
        printTasks(tasks1)
        //
        println()
        println("word-translate -> word-spelling")
        printTasks(tasks2)
        //
        println()
        println("word-sound -> word-translate")
        printTasks(tasks3)
        //
        println()
        println("word-sound -> word-spelling")
        printTasks(tasks4)
    }

    @Test
    void createSaveTask_forItem() {
        TaskGenerator taskCreator = mdb.create(TaskGeneratorImpl)

        //
        Collection<DataBox> tasks1 = taskCreator.createTasks(idItem2, "word-spelling", "word-translate")
        Collection<DataBox> tasks2 = taskCreator.createTasks(idItem2, "word-translate", "word-spelling")
        Collection<DataBox> tasks3 = taskCreator.createTasks(idItem2, "word-sound", "word-translate")
        Collection<DataBox> tasks4 = taskCreator.createTasks(idItem2, "word-sound", "word-spelling")

        //
        Task_upd upd = mdb.create(Task_upd)
        for (DataBox task : tasks1) {
            upd.saveTask(task)
        }
        for (DataBox task : tasks2) {
            upd.saveTask(task)
        }
        for (DataBox task : tasks3) {
            upd.saveTask(task)
        }
        for (DataBox task : tasks4) {
            upd.saveTask(task)
        }
    }


    @Test
    void createTask_spelling() {
        String word = "different"
        //String word = "break in"
        //
        long idItem = mdb.loadQueryRecord("select * from Item where Item.value = :value", [value: word]).getLong("id")

        //
        TaskGeneratorImpl taskCreator = mdb.create(TaskGeneratorImpl)

        //
        Collection<DataBox> tasks = taskCreator.createTasks(idItem, "word-spelling", "word-translate")

        //
        println()
        printTasks(tasks)
    }


    @Test
    void createTask_sound() {
        String word = "different"
        long idItem = mdb.loadQueryRecord("select * from Item where Item.value = :value", [value: word]).getLong("id")

        //
        TaskGenerator taskCreator = mdb.create(TaskGeneratorImpl)

        //
        Collection<DataBox> tasks = taskCreator.createTasks(idItem, "word-sound", "word-translate")

        //
        println()
        printTasks(tasks)
    }

    @Test
    void saveTask_spelling() {
        String word = "simple"
        long idItem = mdb.loadQueryRecord("select * from Item where Item.value = :value", [value: word]).getLong("id")

        //
        TaskGenerator taskCreator = mdb.create(TaskGeneratorImpl)
        Task_upd upd = mdb.create(Task_upd)

        //
        Collection<DataBox> tasks = taskCreator.createTasks(idItem, "word-spelling", "word-translate")

        //
        println()
        printTasks(tasks)

        //
        for (DataBox task : tasks) {
            upd.saveTask(task)
        }
    }

    @Test
    void saveTask_sound() {
        String word = "different"
        long idItem = mdb.loadQueryRecord("select * from Item where Item.value = :value", [value: word]).getLong("id")

        //
        TaskGenerator taskCreator = mdb.create(TaskGeneratorImpl)
        Task_upd upd = mdb.create(Task_upd)

        //
        Collection<DataBox> tasks = taskCreator.createTasks(idItem, "word-sound", "word-translate")

        //
        println()
        printTasks(tasks)

        //
        for (DataBox task : tasks) {
            upd.saveTask(task)
        }
    }

    @Test
    void createSaveTasks() {
        sw.start("total")


        //
        long idItem = 1010
        for (int i = 0; i < 3; i++) {
            createSaveTask(idItem, "word-spelling", "word-translate", 100)
            createSaveTask(idItem, "word-translate", "word-spelling", 100)
            createSaveTask(idItem, "word-sound", "word-translate", 3)
            createSaveTask(idItem, "word-sound", "word-spelling", 3)

            //
            idItem = idItem + 1
        }


        //
        sw.stop("total")
        sw.printItems()
    }


    void createSaveTask(long idItem, String dataTypeQuestion, String dataTypeAnswer, int limit) {
        TaskGenerator taskCreator = mdb.create(TaskGeneratorImpl)
        Task_upd upd = mdb.create(Task_upd)

        try {
            sw.start("create")

            Collection<DataBox> tasks = taskCreator.createTasks(idItem, dataTypeQuestion, dataTypeAnswer, limit)

            sw.setValuePlus("create", "count", 1)
            sw.stop("create")

            //
            printTasks(tasks)
            printTasksOneLine(tasks)

            //
            sw.start("write")

            //
            for (DataBox task : tasks) {
                upd.saveTask(task)
                sw.setValuePlus("write", "count", 1)
            }

            sw.stop("write")

        } catch (Exception e) {
            println(e.message)
        }

    }


}
