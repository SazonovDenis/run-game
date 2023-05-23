package run.game.dao.game;

import jandcode.core.dbm.std.*;

import java.util.*;

/**
 * Основное Api игрового процесса
 */
public interface Server {


    /**
     * Начать раунд по плану
     *
     * @param idPaln план
     * @return игра
     */
    long gameStart(long idPaln);


    /**
     * Выдать очередное задание из готового списка, подготовленного для пользователя.
     * Выбирает те задания, которые еще не выполнялись.
     *
     * @param idPaln план
     * @return задание
     */
    DataBox choiceTask(long idPaln);


    /**
     * Принять ответ пользователя на задание.
     *
     * @param idUsrTask  id ранее выданного задания UsrTask.id)
     * @param taskResult состояние ответа на задание (wasTrue, wasFalse, wasHint, wasSkip)
     */
    void postTaskAnswer(long idUsrTask, Map taskResult);


}
