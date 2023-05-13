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
    void createTask_spelling1() {
        String word = "different"
        long idItem = mdb.loadQueryRecord("select * from Item where Item.value = :value", [value: word]).getLong("id")

        //
        TaskGeneratorImpl taskCreator = mdb.create(TaskGeneratorImpl)

        //
        for (int i = 0; i < 5; i++) {
            DataBox task = taskCreator.createTask(idItem, "word-spelling", "word-translate")

            //
            println()
            printTask(task)
        }
    }

    @Test
    void createTask_spelling2() {
        String word = "break in"
        long idItem = mdb.loadQueryRecord("select * from Item where Item.value = :value", [value: word]).getLong("id")

        //
        TaskGenerator taskCreator = mdb.create(TaskGeneratorImpl)

        //
        for (int i = 0; i < 5; i++) {
            DataBox task = taskCreator.createTask(idItem, "word-spelling", "word-translate")

            //
            println()
            printTask(task)
        }
    }

    @Test
    void createTask_sound() {
        String word = "different"
        long idItem = mdb.loadQueryRecord("select * from Item where Item.value = :value", [value: word]).getLong("id")

        //
        TaskGenerator taskCreator = mdb.create(TaskGeneratorImpl)

        //
        for (int i = 0; i < 5; i++) {
            DataBox task = taskCreator.createTask(idItem, "word-sound", "word-translate")

            //
            println()
            printTask(task)
        }
    }

    @Test
    void saveTask_spelling() {
        String word = "simple"
        long idItem = mdb.loadQueryRecord("select * from Item where Item.value = :value", [value: word]).getLong("id")

        //
        TaskGenerator taskCreator = mdb.create(TaskGeneratorImpl)
        Task_upd upd = mdb.create(Task_upd)

        //
        DataBox task = taskCreator.createTask(idItem, "word-spelling", "word-translate")

        //
        println()
        printTask(task)

        //
        upd.saveTask(task)
    }

    @Test
    void saveTask_sound() {
        String word = "different"
        long idItem = mdb.loadQueryRecord("select * from Item where Item.value = :value", [value: word]).getLong("id")

        //
        TaskGenerator taskCreator = mdb.create(TaskGeneratorImpl)
        Task_upd upd = mdb.create(Task_upd)

        //
        DataBox task = taskCreator.createTask(idItem, "word-sound", "word-translate")

        //
        println()
        printTask(task)

        //
        upd.saveTask(task)
    }

    @Test
    void createSaveTasks_spelling() {
        TaskGenerator taskCreator = mdb.create(TaskGeneratorImpl)
        Task_upd upd = mdb.create(Task_upd)

        //
        long idItem = 1001
        for (int j = 0; j < 20; j++) {
            for (int i = 0; i < 5; i++) {
                try {
                    //
                    DataBox task = taskCreator.createTask(idItem, "word-spelling", "word-translate")

                    //
                    upd.saveTask(task)

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

    @Test
    void createSaveTasks_sound() {
        TaskGenerator taskCreator = mdb.create(TaskGeneratorImpl)
        Task_upd upd = mdb.create(Task_upd)

        //
        long idItem = 2001
        for (int j = 0; j < 20; j++) {
            for (int i = 0; i < 5; i++) {
                try {
                    //
                    DataBox task = taskCreator.createTask(idItem, "word-sound", "word-translate")

                    //
                    upd.saveTask(task)

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

}
