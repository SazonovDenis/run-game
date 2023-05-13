package run.game.dao.game

import jandcode.commons.*
import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

class Server_Test extends RgmBase_Test {


    @Test
    void serializeTask() {
        // Грузим задание
        Server upd = mdb.create(ServerImpl)
        DataBox task = upd.choiceTask(9999)

        //
        println()
        printTask(task)

        //
        println()
        println("task.jsonStr")
        String taskJson = UtJson.toJson(task)
        println(taskJson)

        //
        println()
        println("task.json")
        utils.outMap(UtJson.fromJson(taskJson))
    }

    @Test
    void choiceTask() {
        Server upd = mdb.create(ServerImpl)
        DataBox task = upd.choiceTask(9999)

        //
        println()
        printTask(task)
    }

    @Test
    void postTaskAnswer() {
        //
        println("UsrTask")
        mdb.outTable(mdb.loadQuery("select * from UsrTask order by taskDt"))
        println()


        // Получаем задание
        Server upd = mdb.create(ServerImpl)
        DataBox task = upd.choiceTask(9999)


        // Печатаем задание
        printTask(task)


        // Пользователь отвечает
        StoreRecord recUsrTask = task.get("usrTask")
        long idUsrTask = recUsrTask.getLong("id")
        //
        Store stTaskOption = task.get("taskOption")
        Rnd rnd = new RndImpl()
        long idTaskOption = stTaskOption.get(rnd.num(0, stTaskOption.size() - 1)).getLong("id")


        // Отправляем ответ пользователя
        upd.postTaskAnswer(idUsrTask, idTaskOption)


        //
        println()
        mdb.outTable(mdb.loadQuery("select * from UsrTask order by taskDt"))
    }


    @Test
    void getTask_rpc() throws Exception {
        Map res = apx.execJsonRpc("api", "m/Game/choiceTask", [1001])
        utils.outMap(res)
    }


}
