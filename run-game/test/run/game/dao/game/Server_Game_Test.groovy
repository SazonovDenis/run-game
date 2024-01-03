package run.game.dao.game

import jandcode.commons.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

class Server_Game_Test extends RgmBase_Test {

    boolean doPauseBeforeAnswer = true
    boolean doPauseBeforeGame = true

    @Test
    void getPlans() {
        //
        Server srv = mdb.create(ServerImpl)
        Store st = srv.getPlans()

        //
        println()
        println("Plans")
        mdb.outTable(st)
    }


    @Test
    void getPlan() {
        long idPlan = 1000
        //
        Server srv = mdb.create(ServerImpl)
        StoreRecord st = srv.getPlan(idPlan)

        //
        println()
        println("Plan: " + idPlan)
        mdb.outTable(st)
    }


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
    void closeActiveGame() {
        Server upd = mdb.create(ServerImpl)

        // Закроем все игры
        upd.closeActiveGame()
    }

    @Test
    void testGameProcess() {
        long idPlan = 1000

        // Статистика по уровню
        printTaskStatisticByPlan(idPlan)

        // Игра
        doGameProcess(idPlan)

        // Обновленная статистика по уровню
        printTaskStatisticByPlan(idPlan)
    }

    @Test
    void testGameProcess_xN() {
        int countN = 10
        long idPlan = 1000

        // Статистика по уровню
        printTaskStatisticByPlan(idPlan)

        // Много игр
        for (int i = 0; i < countN; i++) {
            // Игра
            doGameProcess(idPlan)

            //
            if (doPauseBeforeGame) {
                Thread.sleep(rnd.num(150, 500))
            }

            // Текущая статистика по уровню
            if (i % 10 == 0) {
                printTaskStatisticByPlan(idPlan)
            }
        }

        // Итоговая статистика по уровню
        printTaskStatisticByPlan(idPlan)
    }


    @Test
    void testGameProcess_3() {
        //
        doGameProcess(1000)

        doGameProcess(1001)
        doGameProcess(1001)
        doGameProcess(1001)

        doGameProcess(1002)
        doGameProcess(1002)
        doGameProcess(1002)

        doGameProcess(1003, true)
    }

    @Test
    void testGameStatistic() {
        long idPlan = 1000
        long idGame = doGameProcess(idPlan)

        Server upd = mdb.create(ServerImpl)
        StoreRecord st = upd.getGame(idGame)

        println()
        println("getGame")
        mdb.outTable(st)
    }

    @Test
    void testGameProcess_1003() {
        for (int i = 1; i <= 50; i++) {
            doGameProcess(1003, true)
        }
    }

    @Test
    void getAndPrintActiveGame() {
        Server upd = mdb.create(ServerImpl)

        // Есть текущая игра?
        StoreRecord recActiveGame = upd.getActiveGame()
        if (recActiveGame != null) {
            long idGame = recActiveGame.getLong("id")
            long idUsr = getCurrentUserId()
            StoreRecord rec = upd.loadGameInternal(idGame, idUsr)

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

    void printTaskStatisticByPlan(long idPlan) {
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)
        Store stTask = statisticManager.getTaskStatisticByPlan(idPlan)
        mdb.outTable(stTask)
    }


    long doGameProcess(long idPlan) {
        return doGameProcess(idPlan, false)
    }

    long doGameProcess(long idPlan, boolean allTaskOk) {
        Server upd = mdb.create(ServerImpl)

        // Закроем все игры
        upd.closeActiveGame()

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
            if (doPauseBeforeAnswer) {
                Thread.sleep(rnd.num(50, 500))
            }

            StoreRecord recTask = task.get("task")
            long idGameTask = recTask.getLong("id")
            boolean wasTrue
            boolean wasFalse
            boolean wasSkip
            boolean wasHint = rnd.bool(1, 3)
            if (allTaskOk) {
                wasTrue = true
                wasFalse = !wasTrue
                wasSkip = false
                wasHint = rnd.bool(1, 10)
            } else if (rnd.bool(5, 1)) {
                wasTrue = true
                wasFalse = !wasTrue
                wasSkip = false
                wasHint = rnd.bool(1, 10)
            } else if (recTask.getLong("task") % 10 == 0) {
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

            // Пользователь иногда не отвечает
            boolean skipPostTaskAnswer = rnd.bool(1, 5)

            // Пользователь отвечает
            if (allTaskOk || !skipPostTaskAnswer) {
                upd.postTaskAnswer(idGameTask, [
                        wasTrue : wasTrue,
                        wasFalse: wasFalse,
                        wasSkip : wasSkip,
                        wasHint : wasHint,
                ])
            }

            //println()
            println("task " + n + "/" + taskUserToDoCount + ", [" + recTask.getLong("task") + "]")
        }

        //
        return idGame
    }


}
