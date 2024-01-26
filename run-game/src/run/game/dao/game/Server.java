package run.game.dao.game;

import jandcode.core.dbm.std.*;

import java.util.*;

/**
 * Основное Api игрового процесса
 */
public interface Server {


    /**
     * Выдать текущую (незаконченную) игру
     *
     * @return игра
     */
    DataBox getActiveGame();


    /**
     * Завершить текущую (незаконченную) игру
     */
    void closeActiveGame();


    /**
     * Начать игру (раунд) по указанному плану
     *
     * @param idPlan план
     * @return игра
     */
    DataBox gameStart(long idPlan);


    /**
     * Выдать следующее задание из готового списка, подготовленного для пользователя.
     * Выбирает те задания, которые еще не выдавались.
     *
     * @param idGame игра
     * @return задание
     */
    DataBox choiceTask(long idGame);


    /**
     * Выдать последнее выданное задание для пользователя.
     *
     * @param idGame игра
     * @return задание
     */
    DataBox currentTask(long idGame);


    /**
     * Принять ответ пользователя на задание.
     *
     * @param idGameTask id ранее выданного задания GameTask.id)
     * @param taskResult состояние ответа на задание (wasTrue, wasFalse, wasHint, wasSkip)
     */
    void postTaskAnswer(long idGameTask, Map taskResult);


}
