package run.game.dao.backstage

import jandcode.commons.datetime.*
import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*

class UsrPlan_list extends RgmMdbUtils {


    /**
     * Список связей пользователя и их доступ к плану.
     */
    @DaoMethod
    Store getUsrPlanByPlan(long idPlan) {
        Store st = mdb.createStore("PlanUsrPlan.list")

        long idUsr = getCurrentUsrId()
        XDateTime dt = XDateTime.now()
        Map params = [plan: idPlan, usr: idUsr, dt: dt]
        mdb.loadQuery(st, sql(), params)

        return st
    }


    String sql() {
        return """
select 
    --Usr.id, 
    Usr.login, 
    Usr.text, 
    Link.usrFrom, 
    Link.usrTo, 
    Link.dbeg, 
    Link.dend, 
    Link.linkType, 
    UsrPlan.isAllowed
from
    Link 
    join Usr on (
        Link.usrFrom = Usr.id and 
        Link.usrTo = :usr and 
        Link.confirmState = $RgmDbConst.ConfirmState_accepted and
        Link.dbeg <= :dt and 
        Link.dend > :dt
    )
    left join UsrPlan on (
        UsrPlan.usr = Usr.id and
        UsrPlan.plan = :plan 
    )

"""
    }

}
