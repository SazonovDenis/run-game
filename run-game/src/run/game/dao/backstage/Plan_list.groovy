package run.game.dao.backstage

import jandcode.core.apx.dbm.sqlfilter.*
import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*

class Plan_list extends RgmMdbUtils {

    /**
     * Список планов (уровней) пользователя.
     * С рейтингом, сортированный по некоторому критерию, например по самому отстающему
     * или по запланированному учителем.
     */
    @DaoMethod
    Store getPlans() {
        Map params = [isPrivate: true, isHidden: false]
        return getPlansUsr(params)
    }

    /**
     * Список планов (уровней) пользователя.
     * С рейтингом, сортированный по некоторому критерию, например по самому отстающему
     * или по запланированному учителем.
     */
    @DaoMethod
    Store getPlansUsr() {
        Map params = [isPrivate: true, isHidden: false]
        return getPlansUsr(params)
    }

    /**
     * Список общедоступных планов (уровней),
     * не еще не добавленных к списку планов пользователя.
     */
    @DaoMethod
    Store getPlansPublic() {
        Map params = [isPublic: true, isHidden: false]
        return getPlansUsr(params)
    }


    Store getPlansUsr(Map params) {
        Store res = mdb.createStore("Plan.list.statistic")

        //
        long idUsr = getCurrentUserId()
        params.put("usr", idUsr)

        //
        SqlFilter filter = SqlFilter.create(mdb, sqlPlansUsr(), params)
        //
        filter.addWhere("isHidden", "equal")
        //
        SqlFilterBuilder part_public = { SqlFilterContext ctx ->
            ctx.addPart("accessMode", "and (isAuthor = 0 and isAllowed = 0 and isPublic = 1)")
        }
        filter.addWhere("isPublic", part_public)
        //
        SqlFilterBuilder part_private = { SqlFilterContext ctx ->
            ctx.addPart("accessMode", "and (isAuthor = 1 or isAllowed = 1)")
        }
        filter.addWhere("isPrivate", part_private)

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
    Plan.text, 
    coalesce(UsrPlan.isAuthor, 0) isAuthor,
    coalesce(UsrPlan.isHidden, 0) isHidden,
    coalesce(UsrPlan.isAllowed, 0) isAllowed,
    (case when PlanTag_access_public.tag is null then 0 else 1 end) isPublic,
    Cube_UsrPlan.count,
    Cube_UsrPlan.countFull,
    Cube_UsrPlan.ratingTask,
    Cube_UsrPlan.ratingQuickness
    
from
    Plan
    left join PlanTag PlanTag_access_public on (Plan.id = PlanTag_access_public.plan and PlanTag_access_public.tag = ${RgmDbConst.Tag_access_public})
    left join UsrPlan on (Plan.id = UsrPlan.plan and UsrPlan.usr = :usr)
    left join Cube_UsrPlan on (Plan.id = Cube_UsrPlan.plan and UsrPlan.usr = :usr)
)

select 
    * 
from
    Tab_Plans
where
    (1=1) and
    (isAuthor = 1 or isAllowed = 1 or isPublic = 1)
    /*part:accessMode*/    
"""
    }

/*
    (
    UsrPlan.isHidden is null or
    UsrPlan.isHidden = 0
    ) and
    (
    PlanTag_access_public.tag is not null or
    UsrPlan.isAuthor = 1 or
    UsrPlan.isAllowed = 1
    )
*/


}
