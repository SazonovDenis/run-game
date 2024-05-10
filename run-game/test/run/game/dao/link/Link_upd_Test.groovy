package run.game.dao.link

import jandcode.core.auth.std.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

class Link_upd_Test extends RgmBase_Test {


    long usr1 = 1017
    long usr2 = 1018
    String usr1_str = "user1017"
    String usr2_str = "user1018"

    void setUp() throws Exception {
        super.setUp()

        //
        mdb.execQuery("delete from Link where usrFrom in (" + usr1 + "," + usr2 + ")")
        mdb.execQuery("delete from Link where usrTo in (" + usr1 + "," + usr2 + ")")
    }

    @Test
    void test_for_1010() {
        Link_upd upd = mdb.create(Link_upd)


        //
        String usrMe_str = "user1010"
        long usrMe = 1010


        // ---
        // Печатаем
        printLinks([usrMe_str, usr1_str, usr2_str])


        // ---
        setCurrentUser(new DefaultUserPasswdAuthToken(usrMe_str, null))
        println()
        println("CurrentUser: " + authSvc.getCurrentUser().attrs)


        // ---
        // Запросим добавление в "друзья"
        println()
        println("Запросим добавление в 'друзья': " + getCurrentUserId() + " -> " + usr1)
        Map link = [usrTo: usr1, linkType: RgmDbConst.LinkType_friend]
        upd.request(link)


        // ---
        setCurrentUser(new DefaultUserPasswdAuthToken(usr2_str, null))
        println()
        println("CurrentUser: " + authSvc.getCurrentUser().attrs)


        // ---
        // Запросим добавление в "друзья"
        println()
        println("Запросим добавление в 'друзья': " + getCurrentUserId() + " -> " + usrMe)
        link = [usrTo: usrMe, linkType: RgmDbConst.LinkType_friend]
        upd.request(link)

        

        // ---
        // Печатаем
        printLinks([usrMe_str, usr1_str, usr2_str])
    }


    @Test
    void test_request_cancel() {
        Link_upd upd = mdb.create(Link_upd)


        // ---
        // Печатаем
        printLinks([usr1_str, usr2_str])


        // ---
        setCurrentUser(new DefaultUserPasswdAuthToken(usr2_str, null))
        println()
        println("CurrentUser: " + authSvc.getCurrentUser().attrs)


        // ---
        // Запросим добавление в "друзья"
        println()
        println("Запросим добавление в 'друзья': " + getCurrentUserId() + " -> " + usr1)
        Map link = [usrTo: usr1, linkType: RgmDbConst.LinkType_friend]
        upd.request(link)


        // ---
        // Печатаем
        printLinks([usr1_str, usr2_str])


        // ---
        // Отменим запрос
        link = [usrTo: usr1]
        upd.cancel(link)


        // ---
        // Печатаем
        printLinks([usr1_str, usr2_str])
    }


    @Test
    void test_request_confirm() {
        //
        Link_upd upd = mdb.create(Link_upd)


        // ---
        // Печатаем
        printLinks([usr1_str, usr2_str])

        // ---
        println()
        println("CurrentUser: " + authSvc.getCurrentUser().attrs)


        // ---
        // Запросим добавление в "друзья"
        setCurrentUser(new DefaultUserPasswdAuthToken(usr1_str, null))
        println()
        println("CurrentUser: " + authSvc.getCurrentUser().attrs)
        //
        println()
        println("Запросим добавление в 'друзья': " + getCurrentUserId() + "-> " + usr2)
        Map link = [usrTo: usr2, linkType: RgmDbConst.LinkType_friend]
        upd.request(link)


        // ---
        // Печатаем
        printLinks([usr1_str, usr2_str])


        // ---
        // Примем запрос
        setCurrentUser(new DefaultUserPasswdAuthToken(usr2_str, null))
        println()
        println("CurrentUser: " + authSvc.getCurrentUser().attrs)
        //
        println()
        println("Принять запрос добавление в 'друзья'")
        link = [usrFrom: usr1]
        upd.accept(link)


        // ---
        // Печатаем
        printLinks([usr1_str, usr2_str])
    }


    void printLinks(Collection<String> usrs) {
        Link_list list = mdb.create(Link_list)

        //
        for (String usr0 : usrs) {
            setCurrentUser(new DefaultUserPasswdAuthToken(usr0, null))

            //
            Store st = list.getLinks()
            mdb.resolveDicts(st)

            //
            println()
            println("Для пользователя: " + usr0)
            mdb.outTable(st)
        }
    }

}
