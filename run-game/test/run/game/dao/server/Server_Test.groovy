package run.game.dao.server

import jandcode.commons.*
import jandcode.core.apx.test.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.pojo.*
import run.game.dao.pojo.task.*

class Server_Test extends Apx_Test {


    @Test
    void getTask() {
        def upd = mdb.create(Server)
        ServerTask task = upd.getTask(999)

        //
        println()
        println("task")
        println(task)

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
    void getTask_rpc() throws Exception {
        Map res = apx.execJsonRpc("api", "m/Server/getTask", [1001])
        utils.outMap(res)
    }

    @Test
    void getTask_reflect() throws Exception {
        Task task = new Task()
        task.options = new ArrayList<>()

        //
        Store st = mdb.loadQuery("select id, text as value from TagType")
        for (StoreRecord rec : st) {
            TaskElement taskElement = new TaskElement()
            UtReflect.getUtils().setProps(taskElement, rec.getValues())
            task.options.add(taskElement)
        }

        //
        println()
        println("task.json")
        String taskJson = UtJson.toJson(task)
        utils.outMap(UtJson.fromJson(taskJson))
    }

}
