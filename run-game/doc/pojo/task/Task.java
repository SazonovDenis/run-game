package run.game.dao.pojo.task;

import run.game.dao.pojo.fact.*;

import java.util.*;

/**
 * Тестовое задание (вопрос) и варианты ответов
 */
public class Task {

    /**
     * На основе какого факта сформировано задание
     * (проверяли знание этого факта)
     */
    public Fact fact;

    /**
     * Задание (вопрос)
     */
    public TaskElement task;

    /**
     * Варианты ответов
     */
    public Collection<TaskElement> options;

}
