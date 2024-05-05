package run.game.dao.link

import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*

class Link_list extends RgmMdbUtils {


    @DaoMethod
    Store usrFind(String userName) {
        Store st = mdb.createStore("Link.list")

        //
        long idUsr = getCurrentUserId()
        mdb.loadQuery(st, sqlFind(), [usr: idUsr, text: "%" + userName.toLowerCase() + "%"])

        //
        return st
    }

    @DaoMethod
    Store getLinks() {
        Store st = mdb.createStore("Link.list")

        //
        long idUsr = getCurrentUserId()
        mdb.loadQuery(st, sql(), [usr: idUsr])

        //
        return st
    }


    String sqlFind() {
        return """
select 
    Usr.id, 
    Usr.login, 
    Usr.text, 
    Link.linkType, 
    Link.isCommited
from
    Usr left join Link on (Link.link = Usr.id and Link.usr = :usr)
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

    String sql() {
        return """
select 
    Usr.id, 
    Usr.login, 
    Usr.text, 
    Link.linkType, 
    Link.isCommited
from
    Link join Usr on (Link.link = Usr.id and Link.usr = :usr)
order by
    Link.linkType, 
    Usr.text, 
    Usr.login 
"""
    }


}
