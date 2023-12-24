package run.game.dao.game

import groovy.transform.*
import jandcode.commons.datetime.*
import jandcode.core.dao.*
import jandcode.core.store.*
import kis.molap.ntbd.model.cubes.*
import run.game.dao.*

@TypeChecked
public class StatisticManagerImpl extends RgmMdbUtils implements StatisticManager {


    @DaoMethod
    public Store getPlanStatistic() {
        long idUsr = getCurrentUserId()
        XDateTime dt = XDateTime.now().addDays(-5)
        Map params = [usr: idUsr, dt: dt]

        //
        Store res = mdb.createStore("Plan.list.statistic")
        mdb.loadQuery(res, sqlPlanStatistic(), params)

        // Если статистики по плану еще нет - заполнить пессимистичный вариант
        fillDummyTaskInfo(res)

        //
        res.sort("progress")

        //
        return res
    }


    @DaoMethod
    public Store getTaskStatisticByPlan(long idPlan) {
        long idUsr = getCurrentUserId()
        XDateTime dt = XDateTime.now().addDays(-5)
        Map params = [plan: idPlan, usr: idUsr, dt: dt]

        //
        Store res = mdb.createStore("Task.list.statistic")
        mdb.loadQuery(res, sqlTaskStatisticByPlan(), params)

        // Если статистики по task еще нет - заполнить пессимистичный вариант
        fillDummyTaskProgress(res)

        //
        return res
    }


    String sqlPlanStatistic() {
        """           
with Tab_UsrPlanStatistic as (
 
select 
    Plan.id,
    Plan.text,
    Cube_UsrPlan.usr,          
                         
    Cube_Plan.cnt count,
    
    Cube_UsrPlan.progress,
    Cube_UsrPlan.taskInfo

from
    Plan
    left join Cube_Plan on (
        Plan.id = Cube_Plan.plan 
    )     
    left join Cube_UsrPlan on (
        Plan.id = Cube_UsrPlan.plan and 
        Cube_UsrPlan.usr = :usr  
    )     
)

select 
    * 
from 
    Tab_UsrPlanStatistic 
order by
    progress asc,
    text 
"""
    }

    String sqlTaskStatisticByPlan() {
        """
with Tab_UsrTaskStatistic as (

select 
    PlanTask.task,
    Cube_UsrTask.usr,          
    Cube_UsrTask.progress

from
    PlanTask
    left join Cube_UsrTask on (PlanTask.task = Cube_UsrTask.task and Cube_UsrTask.usr = :usr)     

where
    PlanTask.plan = :plan
)                
          
select 
    * 
from 
    Tab_UsrTaskStatistic      
order by
    progress asc,
    task 
"""
    }

    void fillDummyTaskInfo(Store st) {
        for (StoreRecord rec : st) {
            if (rec.isValueNull("progress")) {
                rec.setValue("progress", Cube_UsrPlanStatistic.PROGRESS_MIN)
            }
            if (rec.isValueNull("taskInfo")) {
                List<Map> taskInfoDummy = Cube_UsrPlanStatistic.getTaskInfoDummy()
                Map first = taskInfoDummy.get(0)
                first.put("count", rec.getLong("count"))
                rec.setValue("taskInfo", taskInfoDummy)
            }
        }
    }

    void fillDummyTaskProgress(Store st) {
        for (StoreRecord rec : st) {
            if (rec.isValueNull("progress")) {
                rec.setValue("progress", Cube_UsrPlanStatistic.PROGRESS_MIN)
            }
        }
    }

}
