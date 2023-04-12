package run.game.dao.game;

import jandcode.core.dao.*;
import jandcode.core.dbm.mdb.*;
import run.game.dao.pojo.*;
import run.game.dao.pojo.fact.*;
import run.game.dao.pojo.task.*;
import run.game.dao.pojo.user.*;

import java.util.*;

/**
 * Основное Api игрового сервера
 */
public class Server extends BaseMdbUtils {

    /*
     * =======================================
     * Игровой процесс
     * =======================================
     */

    /**
     * Выдать очередное задание из готового списка, подготовленного для пользователя {@link UserTask}.
     * Выбирает те задания, которые еще не выполнялись.
     *
     * @param id id плана {@link UserPlan}
     * @return {@link ServerTask}
     */
    @DaoMethod
    public ServerTask getTask(long id) {
        ServerTask res = new ServerTask();

        //
        res.id = id;
        res.task = new Task();
        res.task.fact = new Fact();
        res.task.task = new TaskElement();
        res.task.task.value = "TaskElement 990";
        res.task.task.type = new TaskElementType();
        res.task.task.type.text = "new TaskElementType 998";
        res.task.options = new ArrayList<>();
        res.task.options.add(new TaskElement());
        res.task.options.add(new TaskElement());
        TaskElement te = new TaskElement();
        te.type = new TaskElementType();
        te.type.text = "x-t";
        te.value = "xxx";
        res.task.options.add(te);

        //
        return res;
    }

    /**
     * Принять ответ пользователя на задание.
     *
     * @param id     задания (ранее выданного {@link ServerTask})
     * @param result ответ на задание {@link UserAnswer}
     */
    void postTaskAnswer(long id, UserAnswer result) {

    }


}
