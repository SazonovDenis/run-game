package run.game.dao.pojo.user;

import run.game.dao.pojo.fact.*;

import java.util.*;

/**
 * План обучения игрока (уровни для прохождения).
 * Также временные уровни (карты) для дуэлей и турниров.
 */
public class UserPlan {

    /**
     * Название плана (уровня)
     */
    String text;

    /**
     * Формируем план на основе этих тегов.
     * В базе разворачивается как отдельная таблица.
     */
    Collection<Tag> tags;

}
