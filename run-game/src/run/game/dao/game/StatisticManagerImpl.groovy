package run.game.dao.game

import jandcode.core.dao.*
import jandcode.core.dbm.mdb.*
import jandcode.core.store.*

public class StatisticManagerImpl extends BaseMdbUtils implements StatisticManager {


    @DaoMethod
    public Store getFactStatistic(long idFact) {
        return null
    }


    @DaoMethod
    public Map<Long, Store> getPlanStatistic(long idPaln) {
        return null
    }


    /**
     * Выбирает подходящий факт
     */
    public long selectFact(long idPlan) {
        Store stFact = mdb.loadQuery("select id from Fact", [])
        long idFact = stFact.get(0).getLong("id")
        return idFact
    }


    /**
     * Выбирает подходящее задание
     */
    public long selectTask(long idFact) {
        //Store stTask = mdb.loadQuery("select id from Task where factQuestion = :fact", [fact: idFact])
        Store stTask = mdb.loadQuery("select id from Task limit 10", [])
        long idTask = stTask.get(0).getLong("id")
        return idTask
    }

}
