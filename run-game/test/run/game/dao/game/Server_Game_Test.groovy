package run.game.dao.game

import jandcode.commons.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

class Server_Game_Test extends RgmBase_Test {


    @Test
    void serializeTask() {
        // Грузим задание
        Server upd = mdb.create(ServerImpl)
        DataBox task = upd.choiceTask(1000)

        //
        println()
        printTask(task)

        //
        println()
        println("task to jsonStr")
        String strTask = UtJson.toJson(task)
        println(strTask)

        //
        println()
        println("task from jsonStr")
        Map jsonTask = UtJson.fromJson(strTask)
        utils.outMap(jsonTask)
    }


    @Test
    void getTask_rpc() throws Exception {
        Map res = apx.execJsonRpc("api", "m/Game/choiceTask", [1000])
        utils.outMap(res)
    }


    @Test
    void choiceTask() {
        Server upd = mdb.create(ServerImpl)
        DataBox task = upd.choiceTask(1000)

        //
        println()
        printTask(task)
    }


    @Test
    void testLoadGame() {
        Server upd = mdb.create(ServerImpl)

        //
        println()
        println("CurrentUserId: " + upd.getCurrentUserId())

        // Есть текущая игра?
        getAndPrintActiveGame()

        // Закроем
        upd.closeActiveGame()
        //
        println()
        println("ActiveGame closed")

        // Текущая игра
        getAndPrintActiveGame()

        // Стартуем новую игру
        long idPlan = 1000
        StoreRecord recActiveGame = upd.gameStart(idPlan)
        //
        println()
        println("New ActiveGame created")
        mdb.outTable(recActiveGame)

        // Текущая игра
        getAndPrintActiveGame()

        // Закроем
        upd.closeActiveGame()
        //
        println()
        println("ActiveGame closed")
    }

    @Test
    void printPlanStatistic() {
        long idPlan = 1000

        // Статистика по уровню
        printPlanStatistic(idPlan)
    }

    @Test
    void testGameProcess_100() {
        long idPlan = 1000

        // Статистика по уровню
        printPlanStatistic(idPlan)

        // Много игр
        for (int i = 0; i < 100; i++) {
            // Игра
            doGameProcess(idPlan)

            // Текущая статистика по уровню
            if (i % 10 == 0) {
                printPlanStatistic(idPlan)
            }
        }

        // Итоговая статистика по уровню
        printPlanStatistic(idPlan)
    }

    @Test
    void testGameProcess() {
        long idPlan = 1000

        // Статистика по уровню
        printPlanStatistic(idPlan)

        // Игра
        doGameProcess(idPlan)

        // Обновленная статистика по уровню
        printPlanStatistic(idPlan)
    }


    void getAndPrintActiveGame() {
        Server upd = mdb.create(ServerImpl)

        // Есть текущая игра?
        StoreRecord recActiveGame = upd.getActiveGame()
        if (recActiveGame != null) {
            long idGame = recActiveGame.getLong("id")
            StoreRecord rec = upd.loadGame(idGame)

            // Текущая игра
            println()
            println("Current ActiveGame")
            mdb.outTable(rec)
        } else {
            println()
            println("No current ActiveGame")
        }
    }

    void printGameTask(long idGame) {
        println("GameTask, game: " + idGame)
        Store st = mdb.loadQuery("select * from GameTask where game = :game order by dtTask", [game: idGame])
        mdb.outTable(st, 10)
    }

    void printPlanStatistic(long idPlan) {
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)
        Store stTask = statisticManager.getTaskStatisticByPlan(idPlan)
        mdb.outTable(stTask)
    }


    void doGameProcess(long idPlan) {
        Server upd = mdb.create(ServerImpl)

        // Стартуем новую игру
        StoreRecord recActiveGame = upd.gameStart(idPlan)
        long idGame = recActiveGame.getLong("id")
        //
        println()
        println("New ActiveGame created")

        // Текущая игра
        getAndPrintActiveGame()

        //
        int taskUserToDoCount = 8
        for (int n = 1; n <= taskUserToDoCount; n++) {
            // --- Получаем задание
            DataBox task = upd.choiceTask(idGame)
            // printTask(task)
            // mdb.outTable(task.get("task"))

            //
            //Thread.sleep(rnd.num(25, 250))

            // Пользователь отвечает
            StoreRecord recTask = task.get("task")
            long idGameTask = recTask.getLong("id")
            boolean wasTrue
            boolean wasFalse
            boolean wasSkip
            boolean wasHint = rnd.bool(1, 3)
            if (recTask.getLong("task") % 10 == 0) {
                wasTrue = true
                wasFalse = !wasTrue
                wasSkip = false
            } else if (recTask.getLong("task") % 5 == 0) {
                wasTrue = false
                wasFalse = !wasTrue
                wasSkip = false
            } else {
                wasTrue = false
                wasFalse = false
                wasSkip = rnd.bool(1, 5)
                if (!wasSkip) {
                    wasTrue = rnd.bool(2, 1)
                    wasFalse = !wasTrue
                }
            }
            upd.postTaskAnswer(idGameTask, [
                    wasTrue : wasTrue,
                    wasFalse: wasFalse,
                    wasSkip : wasSkip,
                    wasHint : wasHint,
            ])

            //println()
            println("task " + n + "/" + taskUserToDoCount + ", [" + recTask.getLong("task") + "]")
        }
    }


}
