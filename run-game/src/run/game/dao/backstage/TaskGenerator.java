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
public interface TaskGenerator {

    /**
     * Создает задание и неправильные варианты ответа к нему.
     *
     * @param idFactQuestion Факт - вопрос
     * @param idFactAnswer   Факт - ответ
     * @return {task: rec, options: [rec]}
     */
    public DataBox createTask(long idFactQuestion, long idFactAnswer);

}
