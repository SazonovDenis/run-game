package run.game.dao.link

import jandcode.core.auth.std.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

class Link_list_Test extends RgmBase_Test {


    @Test
    void usrFind() {
        //
        Link_list list = mdb.create(Link_list)


        // ---
        // ---
        // ---
        println("CurrentUser: " + authSvc.getCurrentUser().attrs)


        // ---
        String text = "uSER10"
        Store stUsr = list.usrFind(text)
        mdb.resolveDicts(stUsr)
        //
        println()
        println("find: " + text)
        mdb.outTable(stUsr)


        // ---
        text = "пАп"
        stUsr = list.usrFind(text)
        mdb.resolveDicts(stUsr)
        //
        println()
        println("find: " + text)
        mdb.outTable(stUsr)


        // ---
        // ---
        // ---
        setCurrentUser(new DefaultUserPasswdAuthToken("user1014", null))
        println()
        println("CurrentUser: " + authSvc.getCurrentUser().attrs)


        // ---
        text = "uSER10"
        stUsr = list.usrFind(text)
        mdb.resolveDicts(stUsr)
        //
        println()
        println("find: " + text)
        mdb.outTable(stUsr)


        // ---
        text = "пАп"
        stUsr = list.usrFind(text)
        mdb.resolveDicts(stUsr)
        //
        println()
        println("find: " + text)
        mdb.outTable(stUsr)
    }

    @Test
    void usrFind_user() {
        //
        Link_list list = mdb.create(Link_list)

        // ---
        setCurrentUser(new DefaultUserPasswdAuthToken("user1017", null))
        println()
        println("CurrentUser: " + authSvc.getCurrentUser().attrs)

        // 
        String text = "uSER"
        Store stUsr = list.usrFind(text)
        mdb.resolveDicts(stUsr)
        //
        println()
        println("find: " + text)
        mdb.outTable(stUsr)


        // ---
        setCurrentUser(new DefaultUserPasswdAuthToken("user1018", null))
        println()
        println("CurrentUser: " + authSvc.getCurrentUser().attrs)

        //
        text = "uSER"
        stUsr = list.usrFind(text)
        mdb.resolveDicts(stUsr)
        //
        println()
        println("find: " + text)
        mdb.outTable(stUsr)
    }


    @Test
    void getLinks() {
        //
        Link_list list = mdb.create(Link_list)


        // ---
        // ---
        // ---
        setCurrentUser(new DefaultUserPasswdAuthToken("user1012", null))
        println()
        println("CurrentUser: " + authSvc.getCurrentUser().attrs)

        //
        Store stUsrLink = list.getLinks()
        //
        println()
        println("Links")
        mdb.resolveDicts(stUsrLink)
        mdb.outTable(stUsrLink)


        // ---
        // ---
        // ---
        setCurrentUser(new DefaultUserPasswdAuthToken("user1016", null))
        println()
        println("CurrentUser: " + authSvc.getCurrentUser().attrs)

        //
        stUsrLink = list.getLinks()
        //
        println()
        println("Links")
        mdb.resolveDicts(stUsrLink)
        mdb.outTable(stUsrLink)
    }


}
