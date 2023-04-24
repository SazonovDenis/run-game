package run.game.dao.game;

import jandcode.core.dbm.std.*;
import jandcode.core.store.*;

import java.util.*;

/**
 * Основное Api игрового процесса
 */
public interface Server {


    /*
     * =======================================
     * Игровой процесс
     * =======================================
     */

    /**
     * Выдать очередное задание из готового списка, подготовленного для пользователя.
     * Выбирает те задания, которые еще не выполнялись.
     *
     * @param idPaln id плана
     * @return Задание
     */
    DataBox choiceTask(long idPaln);


    /**
     * Принять ответ пользователя на задание.
     *
     * @param idUsrTask    id ранее выданного задания UsrTask.id)
     * @param idTaskOption ответ на задание TaskOption.id
     */
    void postTaskAnswer(long idUsrTask, long idTaskOption);


    /*
     * =======================================
     * Мониторинг результатов
     * =======================================
     */

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
