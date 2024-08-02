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
-- Исходящие подтвержденные связи
select 
    Usr.id, 
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
        Link.usrTo = Usr.id and 
        Link.usrFrom = :usr and 
        Link.confirmState = $RgmDbConst.ConfirmState_accepted and
        Link.dbeg <= :dt and 
        Link.dend > :dt
    )
    -- Подключен ли план к связи
    left join UsrPlan on (
        UsrPlan.usr = Usr.id and
        UsrPlan.plan = :plan 
    )

"""
    }

}
