package run.game.dao.game

import jandcode.commons.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

class Server_Test extends RgmBase_Test {


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
        long idGame = 1001

        //
        Server upd = mdb.create(ServerImpl)
        StoreRecord rec = upd.loadGame(idGame)

        //
        mdb.outTable(rec)
    }

    @Test
    void testGameProcess() {
        Server upd = mdb.create(ServerImpl)

        // Стартуем игру
        long idPaln = 1000
        long idGame = upd.gameStart(idPaln)
        //long idGame = 1002


        // Печатаем состав заданий
        println()
        mdb.outTable(mdb.loadQuery("select * from GameTask where game = :game order by dtTask", [game: idGame]))


        // --- Получаем задание #1
        DataBox task = upd.choiceTask(idGame)

        // Печатаем задание
        println()
        printTask(task)


        // Пользователь отвечает
        StoreRecord recTask = task.get("task")
        long idGameTask = recTask.getLong("id")
        upd.postTaskAnswer(idGameTask, [wasTrue: true])


        // Печатаем состав заданий
        println()
        mdb.outTable(mdb.loadQuery("select * from GameTask where game = :game order by dtTask", [game: idGame]))


        // --- Получаем задание #2
        task = upd.choiceTask(idGame)

        // Печатаем задание
        println()
        printTask(task)


        // Пользователь отвечает
        recTask = task.get("task")
        idGameTask = recTask.getLong("id")
        upd.postTaskAnswer(idGameTask, [wasTrue: false])


        // Печатаем состав заданий
        println()
        mdb.outTable(mdb.loadQuery("select * from GameTask where game = :game order by dtTask", [game: idGame]))
    }


}
