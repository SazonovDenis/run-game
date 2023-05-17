package run.game.dao.game;

import jandcode.core.store.*;

import java.util.*;

/*
 * Мониторинг результатов
 */
public interface StatisticManager {


    /**
     * Выдает статистику и успехи заучивания факта.
     *
     * @param idFact id факта
     * @param idUsr пользователь
     */
    //Store getFactStatistic(long idFact, long idUsr);


    /**
     * Выдает статистику и успехи пользователя.
     *
     * @param idUsr пользователь
     */
    Store getUsrStatistic(long idUsr);


    /**
     * Выдает статистику и успехи выполнения каждого факта в плане.
     *
     * @param idPaln id плана
     */
    Map<Long, Store> getPlanStatistic(long idPaln);


}
