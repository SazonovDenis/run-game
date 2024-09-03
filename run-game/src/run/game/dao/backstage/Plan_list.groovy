package run.game.dao.backstage

import jandcode.commons.error.*
import jandcode.core.apx.dbm.sqlfilter.*
import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.util.*

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
     * Список планов (уровней), подключенных к пользователю: личные + подключенные публичные.
     * С рейтингом.
     */
    @DaoMethod
    Store getPlansAttached() {
        Map params = [accessModeAttached: true, isHidden: false]
        return getPlansInternal(params)
    }

    /**
     * Список общедоступных (публичных) планов (уровней).
     * С рейтингом.
     */
    @DaoMethod
    Store getPlansPublic() {
        Map params = [accessModePublic: true, isHidden: false]
        return getPlansInternal(params)
    }

    /**
     * Список планов (уровней), доступных пользователю: личные + все публичные
     * С рейтингом.
     */
    @DaoMethod
    Store getPlansAvailable() {
        Map params = [isHidden: false]
        return getPlansInternal(params)
    }


    Store getPlansInternal(Map params) {
        Store res = mdb.createStore("Plan.list.statistic")

        // ---
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
        filter.addWhere("accessModeAttached", part_visible)

        // Только личные
        SqlFilterBuilder part_privateOnly = { SqlFilterContext ctx ->
            ctx.addPart("accessModeFilter", "and (isOwner = 1)")
        }
        filter.addWhere("accessModePrivateOnly", part_privateOnly)

        //
        filter.load(res)


        // --- Тэги
        fillPlanTags(res, "id")

        //
        return res
    }

    public void fillPlanTags(Store stPlan, String keyField) {
        // Загрузим тэги
        Store stItemTag = mdb.loadQuery(sqlTag("PlanTag", "plan", stPlan.getUniqueValues(keyField)))
        Map<Object, List<StoreRecord>> mapItemsTag = StoreUtils.collectGroupBy_records(stItemTag, "plan")

        // Заполним поле "тэги" (tags)
        for (StoreRecord rec : stPlan) {
            List<StoreRecord> lstRecItemTag = mapItemsTag.get(rec.getLong(keyField))
            if (lstRecItemTag != null) {
                // Превратим список тэгов в Map тегов
                Map<Long, String> mapItemTag = new HashMap<>()
                for (StoreRecord recItemTag : lstRecItemTag) {
                    mapItemTag.put(recItemTag.getLong("tagType"), recItemTag.getString("tagValue"))
                }
                rec.setValue("tags", mapItemTag)
            }
        }
    }

    String sqlTag(String tagTableName, String refName, Set ids) {
        String strIds
        if (ids.size() == 0) {
            strIds = "0"
        } else {
            strIds = ids.join(",")
        }
        return """
select 
    ${tagTableName}.* 
from
    ${tagTableName} 
where
    ${tagTableName}.${refName} in (${strIds})
"""
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
    
    -- Подзапрос Cube_Plan становится важен, 
    -- если у пользователя нет игр в плане и пользователь не скрывал никаких слов
    coalesce(Cube_UsrPlan.count, Cube_Plan.count, 0) wordCount,
    coalesce(Cube_UsrPlan.countFull, 0) wordCountFull,
    coalesce(Cube_UsrPlan.countLearned, 0) wordCountLearned,
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
        PlanTag_access_default.tagType = $RgmDbConst.TagType_plan_access and
        PlanTag_access_default.tagValue = '$RgmDbConst.TagValue_plan_access_default'
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
