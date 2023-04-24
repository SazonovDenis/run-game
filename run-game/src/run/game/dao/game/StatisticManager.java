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
     */
    Store getFactStatistic(long idFact);


    /**
     * Выдает статистику и успехи выполнения каждого факта в плане.
     *
     * @param idPaln id плана
     */
    Map<Long, Store> getPlanStatistic(long idPaln);


}
