package run.game.dao.backstage;


import jandcode.core.dbm.std.*;
import run.game.dao.pojo.fact.*;
import run.game.dao.pojo.task.*;

/**
 * Готовит тестовое задание {@link Task} для проверки факта {@link Fact}.
 * <p>
 * Основное предназначение - придумать неправильные варианты ответов для факта.
 * Для одного факта - несколько заданий с разными неправильными ответами.
 * <p>
 * Для фактов начального уровня (или для самых популярных) задания готовятся ЗАРАНЕЕ,
 * предназначены для всех пользователей, возможно, выверяются вручную специалистами.
 */
public interface TaskCreator {

    /**
     * Создает задание (и неправильные варианты ответа к нему).
     *
     * @param idItem      Дла какой сущности
     * @param tagQuestion Факт какого типа пойдет как вопрос
     * @param tagAnswer   Факт какого типа пойдет как ответ
     * @return {task: rec, options: [rec]}
     */
    DataBox createTask(long idItem, String tagQuestion, String tagAnswer);

    DataBox loadTask(long idTask);

    long saveTask(DataBox task);

}
