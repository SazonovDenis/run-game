package run.game.dao.game;

import jandcode.core.dbm.std.*;

/**
 * Основное Api игрового процесса
 */
public interface Server {


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


}
