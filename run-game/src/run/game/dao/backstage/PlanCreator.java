package run.game.dao.backstage;

import jandcode.core.store.*;

/**
 * Планы
 */
public interface PlanCreator {


    /**
     * Создает план обучения по тегам
     */
    Store createPlan(Store planTag);


    /**
     * Сохраняет план обучения
     */
    long savePlan(StoreRecord plan, Store planFact);


    /**
     * Удаляет план обучения
     */
    void deletePlan(long id);


}
