package run.game.dao.game

import groovy.transform.*
import jandcode.commons.datetime.*
import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*

@TypeChecked
public class StatisticManagerImpl extends RgmMdbUtils implements StatisticManager {


    //
    Rnd rnd = new RndImpl()


    @DaoMethod
    public Store getTaskStatistic() {
        long idUsr = getCurrentUserId()
        XDateTime dt = XDateTime.now().addDays(-5)
        Map params = [usr: idUsr, dt: dt]

        //
        Store res = mdb.createStore("Task.Statistic")
        mdb.loadQuery(res, sqlTaskStatistic(), params)

        //
        return res
    }

    @DaoMethod
    public Store getPlanStatistic() {
        long idUsr = getCurrentUserId()
        XDateTime dt = XDateTime.now().addDays(-5)
        Map params = [usr: idUsr, dt: dt]

        //
        Store res = mdb.createStore("Plan.Statistic")
        mdb.loadQuery(res, sqlPlanStatistic(), params)

        //
        return res
    }

    @DaoMethod
    public Store getTaskStatisticByPlan(long idPlan) {
        long idUsr = getCurrentUserId()
        XDateTime dt = XDateTime.now().addDays(-5)
        Map params = [plan: idPlan, usr: idUsr, dt: dt]

        //
        Store res = mdb.createStore("Task.Statistic")
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
${sqlStatistic()}

select 
    Plan.id,
    Plan.text,
    Tab_TaskStatistic.usr,          
              
    --PlanTask.id as xxx,

    coalesce(avg(Tab_TaskStatistic.answerTime), 0) as answerTime,
    coalesce(sum(Tab_TaskStatistic.cnt), 0) as cnt,
    coalesce(sum(Tab_TaskStatistic.cntTrue), 0) as cntTrue,
    coalesce(sum(Tab_TaskStatistic.cntFalse), 0) as cntFalse,
    coalesce(sum(Tab_TaskStatistic.cntHint), 0) as cntHint,
    coalesce(sum(Tab_TaskStatistic.cntSkip), 0) as cntSkip,
    coalesce(sum(Tab_TaskStatistic.kfcTrue), 0) as kfcTrue,
    coalesce(sum(Tab_TaskStatistic.kfcFalse), 0) as kfcFalse,
    coalesce(sum(Tab_TaskStatistic.kfcHint), 0) as kfcHint,
    coalesce(sum(Tab_TaskStatistic.kfcSkip), 0) as kfcSkip

from
    Plan
    join PlanTask on (Plan.id = PlanTask.plan)
    --PlanTask
    left join Tab_TaskStatistic on (PlanTask.task = Tab_TaskStatistic.task)     

group by
    Plan.id,
    Plan.text,
    --PlanTask.id,
    Tab_TaskStatistic.usr 

order by
    Plan.text, 
    --PlanTask.id,
    kfcTrue asc  
"""
    }

    String sqlTaskStatisticByPlan() {
        """
${sqlStatistic()}
,   

Tab_TaskForPlan as (
select
    PlanTask.task
from
    PlanTask
where
    PlanTask.plan = :plan
)

select 
    Tab_TaskForPlan.task,
    
    Tab_TaskStatistic.usr,          
                 
    coalesce(avg(Tab_TaskStatistic.answerTime), 0) as answerTime,
    coalesce(sum(Tab_TaskStatistic.cnt), 0) as cnt,
    coalesce(sum(Tab_TaskStatistic.cntTrue), 0) as cntTrue,
    coalesce(sum(Tab_TaskStatistic.cntFalse), 0) as cntFalse,
    coalesce(sum(Tab_TaskStatistic.cntHint), 0) as cntHint,
    coalesce(sum(Tab_TaskStatistic.cntSkip), 0) as cntSkip,
    coalesce(sum(Tab_TaskStatistic.kfcTrue), 0) as kfcTrue,
    coalesce(sum(Tab_TaskStatistic.kfcFalse), 0) as kfcFalse,
    coalesce(sum(Tab_TaskStatistic.kfcHint), 0) as kfcHint,
    coalesce(sum(Tab_TaskStatistic.kfcSkip), 0) as kfcSkip

from
    Tab_TaskForPlan
    left join Tab_TaskStatistic on (Tab_TaskForPlan.task = Tab_TaskStatistic.task)     

group by
    Tab_TaskForPlan.task,
    Tab_TaskStatistic.usr 

order by
    kfcTrue asc  
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
}
