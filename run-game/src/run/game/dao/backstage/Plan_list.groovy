package run.game.dao.backstage

import jandcode.commons.error.XError
import jandcode.core.apx.dbm.sqlfilter.*
import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*

class Plan_list extends RgmMdbUtils {


    /**
     * Список собственных планов (уровней) пользователя.
     * С рейтингом, сортированный по некоторому критерию, например по самому отстающему
     * или по запланированному учителем.
     */
    @DaoMethod
    StoreRecord getPlanUsr(long plan) {
        Map params = [isPrivate: true, plan: plan]
        Store st = getPlansInternal(params)

        //
        if (st.size() == 0) {
            throw new XError("У пользователя нет доступа к плану")
        }
        //
        StoreRecord rec = st.get(0)

        //
        return rec
    }

    /**
     * Список собственных планов (уровней) пользователя.
     * С рейтингом, сортированный по некоторому критерию, например по самому отстающему
     * или по запланированному учителем.
     */
    @DaoMethod
    Store getPlansUsr() {
        Map params = [isPrivate: true, isHidden: false]
        return getPlansInternal(params)
    }

    /**
     * Список общедоступных планов (уровней),
     * не еще не добавленных к списку планов пользователя.
     */
    @DaoMethod
    Store getPlansPublic() {
        Map params = [isPublic: true, isHidden: false]
        return getPlansInternal(params)
    }


    Store getPlansInternal(Map params) {
        Store res = mdb.createStore("Plan.list.statistic")

        //
        long idUsr = getCurrentUserId()
        params.put("usr", idUsr)

        //
        SqlFilter filter = SqlFilter.create(mdb, sqlPlansUsr(), params)
        //
        filter.addWhere("isPublic", "equal")
        filter.addWhere("isHidden", "equal")
        filter.addWhere("plan", "equal")

        //
        SqlFilterBuilder part_public = { SqlFilterContext ctx ->
            ctx.addPart("accessMode", "and (isOwner = 0 and isAllowed = 0 and isPublic = 1)")
        }
        filter.addWhere("isNotMy", part_public)

        //
        SqlFilterBuilder part_private = { SqlFilterContext ctx ->
            ctx.addPart("accessMode", "and (isOwner = 1 or isAllowed = 1)")
        }
        filter.addWhere("isPrivate", part_private)

        //
        filter.load(res)

        //
        return res
    }


    private String sqlPlansUsr() {
        return """ 
with 

Tab_Plans as (

select
    Plan.id,
    Plan.id plan,
    Plan.text planText,
     
    Plan.isPublic,
    
    coalesce(UsrPlan.isHidden, 0) isHidden,
    coalesce(UsrPlan.isOwner, 0) isOwner,
    coalesce(UsrPlan.isAllowed, 0) isAllowed,
    
    coalesce(Cube_UsrPlan.count, 0) count,
    coalesce(Cube_UsrPlan.countFull, 0) countFull,
    coalesce(Cube_UsrPlan.ratingTask, 0) ratingTask,
    coalesce(Cube_UsrPlan.ratingQuickness, 0) ratingQuickness
    
from
    Plan
    left join UsrPlan on (Plan.id = UsrPlan.plan and UsrPlan.usr = :usr)
    left join Cube_UsrPlan on (Plan.id = Cube_UsrPlan.plan and UsrPlan.usr = :usr)
)

select 
    * 
from
    Tab_Plans
where
    (1=1) and
    (isOwner = 1 or isAllowed = 1 or isPublic = 1)
    /*part:accessMode*/    
"""
    }


}