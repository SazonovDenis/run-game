package run.game.dao.game


import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

class Server_Plan_Test extends RgmBase_Test {

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

}
