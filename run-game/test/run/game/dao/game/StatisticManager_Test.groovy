package run.game.dao.game


import org.junit.jupiter.api.*
import run.game.dao.*

class StatisticManager_Test extends RgmBase_Test {


    /**
     * Статистика по планам для пользователя
     */
    @Test
    void loadPlanStatistic() {
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)

        //
        mdb.outTable(statisticManager.getPlanStatistic())
    }

    /**
     * Статистика заданий в плане для пользователя
     */
    @Test
    void loadTaskStatisticByPlan() {
        long idPlan

        //
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)

        //
        for (idPlan = 1000; idPlan <= 1010; idPlan++) {
            println()
            println("idPlan: " + idPlan)
            mdb.outTable(statisticManager.getTaskStatisticByPlan(idPlan))
        }
    }


    /**
     * Статистика по планам для пользователя
     */
    @Test
    void getUsrStatisticByPlan() {
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)
    }


    /**
     * Статистика для пользователя
     */
    @Test
    void loadUsrStatistic() {
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)
    }


}
