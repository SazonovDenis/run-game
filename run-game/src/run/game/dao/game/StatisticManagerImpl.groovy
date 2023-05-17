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

    @DaoMethod
    public Store getUsrStatistic(long idUsr) {
        XDateTime dt = XDateTime.now().addDays(-5)
        return mdb.loadQuery(sqlUsrStatistic(), [usr: idUsr, dt: dt])
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


    /**
     * Выбирает подходящий факт.
     * Факт выбирается с учетом статистики пользователя.
     */
    public long selectFact(long idPlan) {
        long idUsr = getCurrentUserId()

        /////////////////////////
        /////////////////////////
        /////////////////////////
        /////////////////////////
        return 0

        Store stFact = mdb.loadQuery(sqlFact(), [plan: idPlan])
        int n = rnd.num(0, stFact.size() - 1)
        long idFact = stFact.get(n).getLong("id")
        return idFact
    }


    /**
     * Выбирает подходящее задание
     */
    public long selectTask(long idFact) {
        long idUsr = getCurrentUserId()

        ////////////////////////////
        ////////////////////////////
        ////////////////////////////
        ////////////////////////////
        //Store stTask = mdb.loadQuery("select id from Task where factQuestion = :fact", [fact: idFact])
        Store stTask = mdb.loadQuery("select id from Task --limit 100", [])
        int n = rnd.num(0, stTask.size() - 1)
        long idTask = stTask.get(n).getLong("id")

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
