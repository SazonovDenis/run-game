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
        long idUsr = getCurrentUsrId()
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
        long idUsr = getCurrentUsrId()
        XDateTime dt = XDateTime.now()
        mdb.loadQuery(st, sqlList(), [usr: idUsr, dt: dt])

        //
        return st
    }

    @DaoMethod
    Store getLinksToWaiting() {
        long idUsr = getCurrentUsrId()

        //
        Store st = this.loadLinksToWaiting(idUsr)

        //
        return st
    }

    Store loadLinksToWaiting(long idUsr) {
        Store st = mdb.createStore("Link.list")

        //
        XDateTime dt = XDateTime.now()
        mdb.loadQuery(st, sqlLinksTo_Waiting(), [usr: idUsr, dt: dt])

        //
        return st
    }

    Store loadLinksDependent(long idUsr) {
        Store st = mdb.createStore("Link.list")

        //
        XDateTime dt = XDateTime.now()
        mdb.loadQuery(st, sqlLinksFrom_Dependent(), [usr: idUsr, dt: dt])

        //
        return st
    }

    StoreRecord loadLinkDependent(long idUsr, long idUsrTo) {
        XDateTime dt = XDateTime.now()
        StoreRecord rec = mdb.loadQueryRecord(sqlLinkFrom_Dependent(), [usr: idUsr, usrTo: idUsrTo, dt: dt], false)

        //
        return rec
    }


    String sqlFind() {
        return """ 
select 
    Usr.id, 
    Usr.login, 
    Usr.text, 
    -- приоритетом идут данные от LinkFrom (исходящие запросы пользователя)  
    COALESCE(LinkFrom.usrFrom, LinkTo.usrFrom) usrFrom, 
    COALESCE(LinkFrom.usrTo, LinkTo.usrTo) usrTo, 
    COALESCE(LinkFrom.dbeg, LinkTo.dbeg) dbeg, 
    COALESCE(LinkFrom.dend, LinkTo.dend) dend, 
    COALESCE(LinkFrom.linkType, LinkTo.linkType) linkType, 
    COALESCE(LinkFrom.confirmState, LinkTo.confirmState) confirmState
from
    Usr
    left join Link LinkFrom on (
        LinkFrom.usrFrom = :usr and 
        LinkFrom.usrTo = Usr.id and  
        LinkFrom.confirmState <> $RgmDbConst.ConfirmState_cancelled and
        LinkFrom.confirmState <> $RgmDbConst.ConfirmState_refused and
        LinkFrom.dbeg <= :dt and 
        LinkFrom.dend > :dt
    )
    left join Link LinkTo on (
        LinkTo.usrFrom = Usr.id and 
        LinkTo.usrTo = :usr and 
        LinkTo.confirmState <> $RgmDbConst.ConfirmState_cancelled and
        LinkTo.confirmState <> $RgmDbConst.ConfirmState_refused and
        LinkTo.dbeg <= :dt and 
        LinkTo.dend > :dt
    )
where
    Usr.id <> :usr and
    ( 
        lower(login) like :text or
        lower(text) like :text
    )
order by
    text, 
    login 

limit 25    
"""
    }


    String sqlList() {
        return """ 
with list as (
${sqlListBase()}
)

select 
    * 
from
    list
order by
    linkType, 
    confirmState desc, 
    text, 
    login 
"""
    }

    String sqlListBase() {
        return """
-- Исходящие ссылки
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
    Link 
    join Usr on (
        Link.usrTo = Usr.id and 
        Link.usrFrom = :usr and 
        Link.confirmState <> $RgmDbConst.ConfirmState_cancelled and
        Link.dbeg <= :dt and 
        Link.dend > :dt
    )

union


-- Входящие ссылки
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
    Link 
    join Usr on (
        Link.usrFrom = Usr.id and 
        Link.usrTo = :usr and 
        Link.confirmState = $RgmDbConst.ConfirmState_waiting and
        Link.dbeg <= :dt and 
        Link.dend > :dt
    )
"""
    }

    String sqlLinksTo_Waiting() {
        return """
-- Входящие ссылки
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
    Link 
    join Usr on (Link.usrFrom = Usr.id)
where
    Link.usrTo = :usr and 
    Link.confirmState = $RgmDbConst.ConfirmState_waiting and
    Link.dbeg <= :dt and 
    Link.dend > :dt
"""
    }

    String sqlLinksFrom_Base() {
        return """
-- Исходящие ссылки
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
    Link 
    join Usr on (Link.usrFrom = Usr.id)
where
    Link.usrFrom = :usr and
    Link.confirmState = $RgmDbConst.ConfirmState_accepted and
    Link.dbeg <= :dt and 
    Link.dend > :dt
"""
    }

    String sqlLinksFrom_Dependent() {
        return """          
${sqlLinksFrom_Base()}
    and
    Link.linkType in ($RgmDbConst.LinkType_myChild, $RgmDbConst.LinkType_myStudent)
"""
    }


    String sqlLinkFrom_Dependent() {
        return """          
${sqlLinksFrom_Base()}
    and
    Link.usrTo = :usrTo and 
    Link.linkType in ($RgmDbConst.LinkType_myChild, $RgmDbConst.LinkType_myStudent)
"""
    }


}
