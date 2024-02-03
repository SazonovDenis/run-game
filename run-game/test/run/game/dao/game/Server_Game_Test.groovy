package run.game.dao.game

import jandcode.commons.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*
import run.game.dao.backstage.*

class Server_Game_Test extends RgmBase_Test {

    boolean doPauseBeforeAnswer = true
    boolean doPauseBeforeGame = true

    @Test
    void usrPlan_AddDel() {
        Server srv = mdb.create(ServerImpl)
        Plan_list list = mdb.create(Plan_list)
        Plan_upd upd = mdb.create(Plan_upd)

        //
        Store stUsr
        Store stPublic

        //
        stUsr = list.getPlansUsr()
        stPublic = list.getPlansPublic()
        //
        println()
        println("Plans usr")
        mdb.outTable(stUsr)
        println("Plans public")
        mdb.outTable(stPublic)

        // ---
        long idPlan = stPublic.get(0).getLong("id")
        upd.addUsrPlan(idPlan)
        println()
        println("addPlan, idPlan: " + idPlan)

        // ---
        stUsr = list.getPlansUsr()
        stPublic = list.getPlansPublic()
        //
        println()
        println("Plans usr")
        mdb.outTable(stUsr)
        println("Plans public")
        mdb.outTable(stPublic)


        // ---
        upd.delUsrPlan(idPlan)
        println()
        println("delPlan, idPlan: " + idPlan)


        // ---
        stUsr = list.getPlansUsr()
        stPublic = list.getPlansPublic()
        //
        println()
        println("Plans usr")
        mdb.outTable(stUsr)
        println("Plans public")
        mdb.outTable(stPublic)
    }

    @Test
    void usrPlan_hide() {
        Plan_list list = mdb.create(Plan_list)
        Plan_upd upd = mdb.create(Plan_upd)

        //
        Store stUsr
        Store stPublic

        //
        stUsr = list.getPlansUsr()
        stPublic = list.getPlansPublic()
        //
        println()
        println("Plans usr")
        mdb.outTable(stUsr)
        println("Plans public")
        mdb.outTable(stPublic)

        // ---
        long idPlan = stUsr.get(0).getLong("id")
        upd.hideUsrPlan(idPlan)
        println()
        println("hideUsrPlan, idPlan: " + idPlan)

        // ---
        stUsr = list.getPlansUsr()
        stPublic = list.getPlansPublic()
        //
        println()
        println("Plans usr")
        mdb.outTable(stUsr)
        println("Plans public")
        mdb.outTable(stPublic)


        // ---
        upd.unhideUsrPlan(idPlan)
        println()
        println("unhideUsrPlan, idPlan: " + idPlan)


        // ---
        stUsr = list.getPlansUsr()
        stPublic = list.getPlansPublic()
        //
        println()
        println("Plans usr")
        mdb.outTable(stUsr)
        println("Plans public")
        mdb.outTable(stPublic)
    }


    @Test
    void getPlanTaskStatistic() {
        long idPlan = 1000

        //utils.logOn()
        //
        Server srv = mdb.create(ServerImpl)
        DataBox box = srv.getPlanTasks(idPlan)
        mdb.resolveDicts(box)

        //
        println()
        println("Plan: " + idPlan)
        println()
        println("plan")
        mdb.outTable(box.get("plan"))
        println()
        println("planTasks")
        mdb.outTable(box.get("planTasks"))
        println()
        println("statistic")
        mdb.outTable(box.get("statistic"))
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
        long idPlan = 1000
        int countN = 10

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
    void testGameStatistic() {
        long idPlan = 1000
        long idGame = doGameProcess(idPlan)

        Server upd = mdb.create(ServerImpl)
        DataBox res = upd.getGame(idGame)

        println()
        println("getGame")
        mdb.outTable(res)
    }

    @Test
    void getGame() {
        long idGame = 1009

        Server upd = mdb.create(ServerImpl)
        DataBox res = upd.getGame(idGame)

        println()
        println("game")
        mdb.outTable(res.get("game"))

        println()
        println("tasks")
        mdb.outTable(res.get("tasks"))

        println()
        println("statistic")
        mdb.outTable(res.get("statistic"))
    }

    @Test
    void testGameProcess_1000_bad() {
        for (int i = 1; i <= 10; i++) {
            doGameProcess_internal(1000, false, true)
        }
    }

    @Test
    void testGameProcess_1000_good() {
        for (int i = 1; i <= 10; i++) {
            doGameProcess_internal(1000, true, false)
        }
    }

    @Test
    void getAndPrintActiveGame() {
        Server upd = mdb.create(ServerImpl)

        // Есть текущая игра?
        DataBox game = upd.getActiveGame()
        if (game == null) {
            game = upd.getLastGame()
        }
        if (game != null) {
            // Текущая игра
            println()
            println("game")
            mdb.outTable(game.get("game"))
            println()
            println("gameTasks")
            mdb.outTable(game.get("tasks"))
            println()
            println("statistic")
            mdb.outTable(game.get("statistic"))

        } else {
            println()
            println("No active game")
        }
    }

    void printTaskStatisticByPlan(long idPlan) {
        Server srv = mdb.create(ServerImpl)
        DataBox box = srv.getPlanTasks(idPlan)
        mdb.outTable(box.get("tasks"))
    }

    long doGameProcess(long idPlan) {
        return doGameProcess_internal(idPlan, false, false)
    }

    long doGameProcess_internal(long idPlan, boolean allTaskOk, boolean allTaskErr) {
        Server upd = mdb.create(ServerImpl)

        // Закроем все игры
        upd.closeActiveGame()

        // Стартуем новую игру
        DataBox game = upd.gameStart(idPlan)
        StoreRecord recActiveGame = game.get("game")
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

            if (recTask == null) {
                println("Задания кончились")
                break
            }

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
            } else if (allTaskErr) {
                wasTrue = false
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

        // Закроем игру
        upd.closeActiveGame()

        //
        return idGame
    }


}
