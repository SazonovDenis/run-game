package run.game.dao.pojo.user;

import run.game.dao.pojo.fact.*;

import java.util.*;

/**
 * Сформированный план обучения.
 * Для плана отобран набор фактов, которые надо выучить.
 */
public class UserPlanFact {

    /**
     * План (уровень)
     */
    UserPlan plan;

    /**
     * Подходящие факты
     */
    Collection<Fact> facts;

}
