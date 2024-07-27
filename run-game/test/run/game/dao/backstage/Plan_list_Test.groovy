package run.game.dao.backstage

import jandcode.core.auth.std.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

class Plan_list_Test extends RgmBase_Test {


    @Test
    void getPlans() {
        //
        Plan_list list = mdb.create(Plan_list)


        // ---
        Store stUsr = list.getPlansAttached()
        Store stPublic = list.getPlansPublic()
        //
        println()
        println("Plans usr")
        mdb.outTable(stUsr)
        println("Plans public")
        mdb.outTable(stPublic)


        // ---
        setCurrentUser(new DefaultUserPasswdAuthToken("user1010", null))
        println()
        println("CurrentUser: " + authSvc.getCurrentUser().attrs)

        //
        stUsr = list.getPlansAttached()
        stPublic = list.getPlansPublic()
        //
        println()
        println("Plans usr")
        mdb.outTable(stUsr)
        println("Plans public")
        mdb.outTable(stPublic)
    }


}
