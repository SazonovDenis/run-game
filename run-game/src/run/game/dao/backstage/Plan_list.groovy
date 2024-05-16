package run.game.dao.backstage

import jandcode.commons.error.*
import jandcode.core.apx.dbm.sqlfilter.*
import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*

class Plan_list extends RgmMdbUtils {

    /**
     *
     */
    @DaoMethod
    StoreRecord getPlan(long plan) {
        Map params = [plan: plan]
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
     * Список планов (уровней), доступных пользователю.
     * С рейтингом.
     */
    @DaoMethod
    Store getPlansVisible() {
        Map params = [accessModeVisible: true, isHidden: false]
        return getPlansInternal(params)
    }

    /**
     * Список общедоступных планов (уровней).
     * С рейтингом.
     */
    @DaoMethod
    Store getPlansPublic() {
        Map params = [accessModePublic: true, isHidden: false]
        return getPlansInternal(params)
    }


    Store getPlansInternal(Map params) {
        Store res = mdb.createStore("Plan.list.statistic")

        //
        long idUsr = getContextOrCurrentUsrId()
        params.put("usr", idUsr)

        //
        SqlFilter filter = SqlFilter.create(mdb, sqlPlansUsr(), params)

        //
        filter.addWhere("plan", "equal")
        filter.addWhere("isPublic", "equal")
        filter.addWhere("isHidden", "equal")

        // Все публичные планы (включая подключенные)
        SqlFilterBuilder part_public = { SqlFilterContext ctx ->
            ctx.addPart("accessModeFilter", "and (isPublic = 1)")
        }
        filter.addWhere("accessModePublic", part_public)

        // Личные + подключенные публичные
        SqlFilterBuilder part_visible = { SqlFilterContext ctx ->
            ctx.addPart("accessModeFilter", "and (isOwner = 1 or isAllowed = 1)")
        }
        filter.addWhere("accessModeVisible", part_visible)

        // Только личные
        SqlFilterBuilder part_privateOnly = { SqlFilterContext ctx ->
            ctx.addPart("accessModeFilter", "and (isOwner = 1)")
        }
        filter.addWhere("accessModePrivateOnly", part_privateOnly)

        //
        filter.load(res)

        //
        return res
    }


    private String sqlPlansUsr() {
        return """ 
with 

Cube_Plan as (

select 
    PlanFact.plan,
    count(*) as count 
from
    PlanFact
group by
    PlanFact.plan

),


Tab_Plans as (

select
    Plan.id,
    Plan.id plan,
    Plan.text planText,
     
    Plan.isPublic,
    
    coalesce(UsrPlan.isHidden, 0) isHidden,
    coalesce(UsrPlan.isOwner, 0) isOwner,
    coalesce(UsrPlan.isAllowed, 0) isAllowed,
    (case when PlanTag_access_default.plan is null then 0 else 1 end) as isDefault,
    
    coalesce(Cube_UsrPlan.count, Cube_Plan.count, 0) count,
    coalesce(Cube_UsrPlan.countFull, 0) countFull,
    coalesce(Cube_UsrPlan.ratingTask, 0) ratingTask,
    coalesce(Cube_UsrPlan.ratingQuickness, 0) ratingQuickness
    
from
    Plan
    left join UsrPlan on (
        Plan.id = UsrPlan.plan and 
        UsrPlan.usr = :usr
    )
    left join Cube_Plan on (
        Plan.id = Cube_Plan.plan 
    )
    left join Cube_UsrPlan on (
        Plan.id = Cube_UsrPlan.plan and 
        Cube_UsrPlan.usr = :usr
    )
    left join PlanTag PlanTag_access_default on (
        UsrPlan.plan = PlanTag_access_default.plan and
        PlanTag_access_default.tag = $RgmDbConst.Tag_plan_access_default
    ) 
)


select 
    * 
from
    Tab_Plans
where   
    (1=1) and
    -- Обязательный фильтр доступа
    (isOwner = 1 or isAllowed = 1 or isPublic = 1) 
    -- Опциональный фильтр
    /*part:accessModeFilter*/    
"""
    }


}
