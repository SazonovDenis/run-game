package run.game.dao.game;

import jandcode.core.dbm.std.*;
import jandcode.core.store.*;

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
    StoreRecord gameStart(long idPaln);


    /**
     * Выдать очередное задание из готового списка, подготовленного для пользователя.
     * Выбирает те задания, которые еще не выполнялись.
     *
     * @param idGame игра
     * @return задание
     */
    DataBox choiceTask(long idGame);


    /**
     * Принять ответ пользователя на задание.
     *
     * @param idGameTask id ранее выданного задания GameTask.id)
     * @param taskResult состояние ответа на задание (wasTrue, wasFalse, wasHint, wasSkip)
     */
    void postTaskAnswer(long idGameTask, Map taskResult);


}
