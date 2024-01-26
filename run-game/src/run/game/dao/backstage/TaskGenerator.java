package run.game.dao.backstage;


import jandcode.core.dbm.std.*;
import jandcode.core.store.*;
import run.game.dao.pojo.fact.*;
import run.game.dao.pojo.task.*;

import java.util.*;

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
     * Генерит комбинации известных фактов для сущности,
     * ограничивая выборку фактов параметрами dataType и factTag.
     *
     * @param item             Сущность
     * @param dataTypeQuestion
     * @param dataTypeAnswer
     * @param factTagQuestion
     * @param factTagAnswer
     * @return Store с комбинациями фактов (factQuestion + factAnswer)
     */
    Store generateItemFactsCombinations(long item, long dataTypeQuestion, long dataTypeAnswer, Collection<Long> factTagQuestion, Collection<Long> factTagAnswer);


    /**
     * Сгенерить задание для пары фактов
     * Создает задание и неправильные варианты ответа к нему.
     *
     * @param factQuestion      Факт - вопрос
     * @param factAnswer        Факт - ответ на вопрос
     * @param answerFalseValues Если указано, то неправильные варианты берутся не из всего
     *                          корпуса слов, а из этого списка. Это важно, если задание
     *                          генерится для списка слов, связанных определенной логикой.
     *                          Тогда появление среди вариантов ответа слов НЕ связанных этой логикой
     *                          (из произвольного корпуса слов), приведет к тому,
     *                          что правильные ответы легко опопзнаются.
     * @return DataBox с вопросом и ответами (1 запись TaskQuestion, несколько записей TaskOption)
     */
    DataBox generateTask(long factQuestion, long factAnswer, Collection<String> answerFalseValues);


}
