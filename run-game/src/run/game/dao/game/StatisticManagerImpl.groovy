package run.game.dao.game

import groovy.transform.*
import jandcode.commons.datetime.*
import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.dao.*
import jandcode.core.store.*
import kis.molap.ntbd.model.cubes.*
import run.game.dao.*

@TypeChecked
public class StatisticManagerImpl extends RgmMdbUtils implements StatisticManager {


    //
    Rnd rnd = new RndImpl()


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
    public Store getTaskStatistic() {
        long idUsr = getCurrentUserId()
        XDateTime dt = XDateTime.now().addDays(-5)
        Map params = [usr: idUsr, dt: dt]

        //
        Store res = mdb.createStore("Task.list.statistic")
        mdb.loadQuery(res, sqlTaskStatistic(), params)

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

        //
        return res
    }

    String sqlTaskStatistic() {
        """
${sqlStatistic()}

select 
    Tab_TaskStatistic.* 

from
    Tab_TaskStatistic     

order by
    kfcTrue asc

limit 10    
"""
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

    String sqlStatistic() {
        return """
with

Tab_TaskStatisticBase as (
select
    GameTask.usr,
    GameTask.task,
    Task.factQuestion,
    Task.factAnswer,
    avg(extract('epoch' from GameTask.dtAnswer) - extract('epoch' from GameTask.dtTask)) as answerTime,
    count(*) as cnt,
    sum(case when wasTrue = 1 then 1.0 else 0.0 end) as cntTrue,
    sum(case when wasFalse = 1 then 1.0 else 0.0 end) as cntFalse,
    sum(case when wasHint = 1 then 1.0 else 0.0 end) as cntHint,
    sum(case when wasSkip = 1 then 1.0 else 0.0 end) as cntSkip
from
    GameTask
    join Task on (GameTask.task = Task.id)
where
    GameTask.usr = :usr and
    GameTask.dtTask > :dt
group by
    GameTask.usr,
    GameTask.task,
    Task.factQuestion,
    Task.factAnswer
),

Tab_TaskStatistic as (
select 
    Tab_TaskStatisticBase.*,
    (cntTrue) / (cnt) * 100 as kfcTrue,
    (cntFalse) / (cnt) * 100 as kfcFalse,
    (cntHint) / (cnt) * 100 as kfcHint,
    (cntSkip) / (cnt) * 100 as kfcSkip
from
    Tab_TaskStatisticBase
)

"""
    }


    String sqlFact() {
        return """
select
    * 
from
    PlanFact
where
    PlanFact.plan = :plan
"""

    }

    String sqlFactStatistic() {
        return """
select
    Task.*,
    GameTask.dtTask,
    GameTask.dtAnswer,
    TaskOption.isTrue
from
    GameTask
    join Task on (GameTask.task = Task.id and Task.factQuestion = :fact) 
    join TaskOption on (TaskOption.id = GameTask.answerTaskOption) 
where
    GameTask.usr = :usr
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

}
