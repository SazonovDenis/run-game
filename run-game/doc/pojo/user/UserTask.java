package run.game.dao.pojo.user;

import run.game.dao.pojo.task.*;

import java.util.*;

/**
 * Задания, выданные пользователю (сформированные для пользователя)
 */
public class UserTask {

    long id;

    User user;

    /**
     * Задание
     */
    Task task;

    /**
     * Дата выдачи пользователю
     */
    Date taskDate;

    /**
     * Ответ пользователя
     */
    UserAnswer userResult;

    /**
     * Дата поступления ответа от пользователя
     */
    Date userDate;

    /**
     * Затраченное время
     */
    long userDuration;

}
