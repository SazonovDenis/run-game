package run.game.dao.game;

import jandcode.core.store.*;

/*
 * Мониторинг результатов, учет статистики пользователя.
 */
public interface StatisticManager {

    /**
     * Выдает статистику и успехи каждого Paln.
     */
    Store getPlanStatistic();


    /**
     * Выдает статистику и успехи для каждого Task.
     */
    //Store getTaskStatistic();


    /**
     * Выдает статистику и успехи для каждого Task в плане idPlan.
     *
     * @param idPlan id плана
     */
    Store getTaskStatisticByPlan(long idPlan);


}
