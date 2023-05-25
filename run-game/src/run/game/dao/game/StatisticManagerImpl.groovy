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
    TaskStatistic.* 

from
    TaskStatistic     

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
    TaskStatistic.usr,          
              
    --PlanTask.id as xxx,

    avg(TaskStatistic.answerTime) as answerTime,
    
    sum(TaskStatistic.cnt) as cnt,
    
    sum(TaskStatistic.cntTrue) as cntTrue,
    sum(TaskStatistic.cntFalse) as cntFalse,
    sum(TaskStatistic.cntHint) as cntHint,
    sum(TaskStatistic.cntSkip) as cntSkip,
    sum(TaskStatistic.kfcTrue) as kfcTrue,
    sum(TaskStatistic.kfcFalse) as kfcFalse,
    sum(TaskStatistic.kfcHint) as kfcHint,
    sum(TaskStatistic.kfcSkip) as kfcSkip

from
    Plan
    join PlanTask on (Plan.id = PlanTask.plan)
    --PlanTask
    left join TaskStatistic on (PlanTask.task = TaskStatistic.task)     

group by
    Plan.id,
    Plan.text,
    --PlanTask.id,
    TaskStatistic.usr 

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

TaskForPlan as (
select
    PlanTask.task
from
    PlanTask
where
    PlanTask.plan = :plan
)

select 
    TaskForPlan.task,
    
    TaskStatistic.usr,          

    avg(TaskStatistic.answerTime) as answerTime,
    
    sum(TaskStatistic.cnt) as cnt,
    
    sum(TaskStatistic.cntTrue) as cntTrue,
    sum(TaskStatistic.cntFalse) as cntFalse,
    sum(TaskStatistic.cntHint) as cntHint,
    sum(TaskStatistic.cntSkip) as cntSkip,
    sum(TaskStatistic.kfcTrue) as kfcTrue,
    sum(TaskStatistic.kfcFalse) as kfcFalse,
    sum(TaskStatistic.kfcHint) as kfcHint,
    sum(TaskStatistic.kfcSkip) as kfcSkip

from
    TaskForPlan
    left join TaskStatistic on (TaskForPlan.task = TaskStatistic.task)     

group by
    TaskForPlan.task,
    TaskStatistic.usr 

order by
    kfcTrue asc  
"""
    }

    String sqlStatistic() {
        return """
with

TaskStatisticBase as (
select
    GameTask.usr,
    GameTask.task,
    Task.factQuestion,
    Task.factAnswer,
    avg(GameTask.dtAnswer - GameTask.dtTask) answerTime,
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

TaskStatistic as (
select 
    TaskStatisticBase.*,
    (cntTrue) / (cnt) * 100 as kfcTrue,
    (cntFalse) / (cnt) * 100 as kfcFalse,
    (cntHint) / (cnt) * 100 as kfcHint,
    (cntSkip) / (cnt) * 100 as kfcSkip
from
    TaskStatisticBase
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
