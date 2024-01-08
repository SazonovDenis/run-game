package run.game.dao.game
////////////////////////////
////////////////////////////
////////////////////////////
////////////////////////////
////////////////////////////
//заюзать куб "статистика по Game" в других кубах?
//
// и вообще логика ститстика расползается по двум классам из-за необходимости
// статистику обновлять и получать моментально после ответа на задание,
// при этом есть опасеня, что дергать именно куб - тормознуто.
// Продумать как разделить срочное и несрочное, а если нет, то как разумнее
// делить фунцианал между кубом и утилитными классами

import org.junit.jupiter.api.*
import run.game.dao.*

class StatisticManager_Test extends RgmBase_Test {


    @Test
    void getGameStatisticByPlan() {
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)

        //
        utils.logOn()
        mdb.outTable(statisticManager.getGamesStatisticByPlan(1002))
    }

    @Test
    void getGameStatistic() {
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)

        //
        mdb.outTable(statisticManager.getGameStatistic(1000))
        mdb.outTable(statisticManager.getGameStatistic(1001))
    }


    /**
     * Статистика по планам для пользователя
     */
    @Test
    void loadPlanStatistic() {
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)

        //
        mdb.outTable(statisticManager.getPlansStatistic())
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
