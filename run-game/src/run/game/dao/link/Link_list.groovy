package run.game.dao.link

import jandcode.commons.datetime.*
import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*

class Link_list extends RgmMdbUtils {


    @DaoMethod
    Store usrFind(String userName) {
        Store st = mdb.createStore("Link.list")

        //
        long idUsr = getCurrentUserId()
        XDateTime dt = XDateTime.now()
        String text = "%" + userName.toLowerCase() + "%"
        mdb.loadQuery(st, sqlFind(), [usr: idUsr, dt: dt, text: text])

        //
        return st
    }

    @DaoMethod
    Store getLinks() {
        Store st = mdb.createStore("Link.list")

        //
        long idUsr = getCurrentUserId()
        XDateTime dt = XDateTime.now()
        mdb.loadQuery(st, sqlList(), [usr: idUsr, dt: dt])

        //
        return st
    }


    String sqlFind() {
        return """
select 
    Usr.id, 
    Usr.login, 
    Usr.text, 
    Link.usrFrom, 
    Link.usrTo, 
    Link.dbeg, 
    Link.dend, 
    Link.linkType, 
    Link.confirmState
from
    Usr left join Link on (
        Link.usrTo = Usr.id and 
        Link.usrFrom = :usr and 
        Link.dbeg <= :dt and 
        Link.dend > :dt
    )
where
    lower(Usr.login) like :text or
    lower(Usr.text) like :text
order by
    Link.linkType, 
    Usr.text, 
    Usr.login 
limit 25    
"""
    }

    String sqlList() {
        return """
select 
    Usr.id, 
    Usr.login, 
    Usr.text, 
    Link.usrFrom, 
    Link.usrTo, 
    Link.dbeg, 
    Link.dend, 
    Link.linkType, 
    Link.confirmState
from
    Link join Usr on (
        Link.usrTo = Usr.id and 
        Link.usrFrom = :usr and 
        Link.dbeg <= :dt and 
        Link.dend > :dt
    )
order by
    Link.linkType, 
    Usr.text, 
    Usr.login 
"""
    }


}
