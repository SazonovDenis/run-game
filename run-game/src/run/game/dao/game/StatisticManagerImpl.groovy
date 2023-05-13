package run.game.dao.game


import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*


public class StatisticManagerImpl extends RgmMdbUtils implements StatisticManager {


    //
    Rnd rnd = new RndImpl()


    @DaoMethod
    public Store getFactStatistic(long idFact) {
        Store st = mdb.loadQuery(sqlFactStatistic(), [usr: getCurrentUserId(), fact: idFact])
        return st
    }


    @DaoMethod
    public Map<Long, Store> getPlanStatistic(long idPaln) {
        return null
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
        Store stTask = mdb.loadQuery("select id from Task limit 100", [])
        int n = rnd.num(0, stTask.size() - 1)
        long idTask = stTask.get(n).getLong("id")
        return idTask
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
