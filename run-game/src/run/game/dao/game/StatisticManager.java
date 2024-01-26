package run.game.dao.game;

import jandcode.core.store.*;

/*
 * Мониторинг результатов, учет статистики пользователя.
 */
public interface StatisticManager {


    /**
     * @return Выдает статистику по играм плана
     */
    Store getGamesStatisticByPlan(long idPlan);


    /**
     * @return Выдает статистику по игре
     */
    StoreRecord getGameStatistic(long idPlan);


    /**
     * Выдает статистику и успехи каждого Plan.
     */
    Store getPlansStatistic();


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
