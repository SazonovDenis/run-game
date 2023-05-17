package run.game.dao.game

import jandcode.commons.datetime.*
import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*

public class StatisticManagerImpl extends RgmMdbUtils implements StatisticManager {


    //
    Rnd rnd = new RndImpl()


    @DaoMethod
    public Map<Long, Store> getPlanStatistic(long idPaln) {
        return null
    }

    public Store getUsrStatistic(long idUsr) {
        XDateTime dt = XDateTime.now().addDays(-5)
        return mdb.loadQuery(sqlUsrStatistic(), [usr: idUsr, dt: dt])
    }

    public Store getUsrStatisticByPlan(long idUsr, long idPlan) {
        XDateTime dt = XDateTime.now().addDays(-5)

        Map params = [usr: idUsr, plan: idPlan, dt: dt]

        return mdb.loadQuery(sqlUsrStatisticByPlan(), params)
    }

    String sqlUsrStatistic() {
        return """
with

s1 as (
select
    UsrTask.usr,
    UsrTask.task,
    Task.factQuestion,
    Task.factAnswer,
    avg(UsrTask.answerDt - UsrTask.taskDt) answerTime,
    count(*) as cnt,
    sum(case when wasTrue = 1 then 1.0 else 0.0 end) as cntTrue,
    sum(case when wasFalse = 1 then 1.0 else 0.0 end) as cntFalse,
    sum(case when wasHint = 1 then 1.0 else 0.0 end) as cntHint,
    sum(case when wasSkip = 1 then 1.0 else 0.0 end) as cntSkip
from
    UsrTask
    join Task on (UsrTask.task = Task.id)
where
    UsrTask.usr = :usr and
    UsrTask.taskDt > :dt
group by
    UsrTask.usr,
    UsrTask.task,
    Task.factQuestion,
    Task.factAnswer
),

s2 as (
select 
    s1.*,
    (cntTrue) / (cnt) * 100 as kfcTrue,
    (cntFalse) / (cnt) * 100 as kfcFalse,
    (cntHint) / (cnt) * 100 as kfcHint,
    (cntSkip) / (cnt) * 100 as kfcSkip
from
    s1
)

select 
    s2.* 
from
    s2     
order by
    kfcTrue asc
"""
    }

    String sqlUsrStatisticByPlan() {
        return """
with

s1 as (
select
    PlanTask.plan,
    UsrTask.usr,
    PlanTask.task,
    Task.factQuestion,
    Task.factAnswer,
    avg(UsrTask.answerDt - UsrTask.taskDt) answerTime,
    count(*) as cnt,
    sum(case when wasTrue = 1 then 1.0 else 0.0 end) as cntTrue,
    sum(case when wasFalse = 1 then 1.0 else 0.0 end) as cntFalse,
    sum(case when wasHint = 1 then 1.0 else 0.0 end) as cntHint,
    sum(case when wasSkip = 1 then 1.0 else 0.0 end) as cntSkip

from
    PlanTask
    left join UsrTask on (UsrTask.task = PlanTask.task and UsrTask.taskDt > :dt and UsrTask.usr = :usr)
    left join Task on (Task.id = UsrTask.task)

where
    PlanTask.plan = :plan
    
group by
    PlanTask.plan,
    UsrTask.usr,
    PlanTask.task,
    Task.factQuestion,
    Task.factAnswer
),

s2 as (
select 
    s1.*,
    (cntTrue) / (cnt) * 100 as kfcTrue,
    (cntFalse) / (cnt) * 100 as kfcFalse,
    (cntHint) / (cnt) * 100 as kfcHint,
    (cntSkip) / (cnt) * 100 as kfcSkip
from
    s1
)

select 
    s2.* 
from
    s2     
order by
    kfcTrue asc

limit 10    
"""
    }


    /**
     * Выбирает подходящее задание.
     * Задание выбирается с учетом статистики пользователя.
     */
    public long selectTask(long idPlan) {
        long idUsr = getCurrentUserId()

        //
        //Store stTaskStaistic = getUsrStatistic(idUsr)
        Store stTaskStaistic = getUsrStatisticByPlan(idUsr, idPlan)
        int n = rnd.num(0, stTaskStaistic.size() - 1)
        long idTask = stTaskStaistic.get(n).getLong("task")

        //idTask = 3116
        return idTask
    }


/*

более полная генерация заданий
(теперь нет встрокенного рандома, можно выбрать все комбинации фактов исчерпывающим образм)
использование тегов

подгрузка TagValue к списку фактов, рефакторинг sqlFactByTagValue

запись и использование статистики

создать планы, нагенерить задания в рамках плана

показывать продвижение внутри плана

связь плана, статистики и тегов


подозрительный вариант ответов

q: spread
распространенный - НЕ ok
распространение - ok
*/


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
    UsrTask.taskDt,
    UsrTask.answerDt,
    TaskOption.isTrue
from
    UsrTask
    join Task on (UsrTask.task = Task.id and Task.factQuestion = :fact) 
    join TaskOption on (TaskOption.id = UsrTask.answerTaskOption) 
where
    UsrTask.usr = :usr
"""

    }
}
