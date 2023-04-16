package run.game.dao.backstage;


import run.game.dao.pojo.fact.*;
import run.game.dao.pojo.task.*;

/**
 * Готовит тестовое задание {@link Task} для проверки факта {@link Fact}.
 * <p>
 * Основное предназначение - придумать неправильные варианты ответов для факта.
 * <p>
 * Для фактов начального уровня (или для самых популярных) задания готовятся ЗАРАНЕЕ,
 * для одного факта - в нескольких экземплярах, предназначены для всех, выверяются вручную специалистами.
 */
public interface TaskCreator {

    Task createTask(Fact fact);

    Task loadTask(long idTask);

}
