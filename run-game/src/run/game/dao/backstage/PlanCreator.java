package run.game.dao.backstage;

import jandcode.core.dbm.std.*;
import jandcode.core.store.*;

import java.util.*;

/**
 * Планы
 */
public interface PlanCreator {


    /**
     * Создает план обучения по тегам
     */
    //DataBox createPlan(Store planTag);

    /**
     * Создает план обучения для набора слов по типам фактов
     */
    long createPlan(String planName, Collection<Long> idItems, long dataTypeQuestion, long dataTypeAnswer, int limit);


    /**
     * Сохраняет план обучения
     */
    long savePlan(StoreRecord plan, Collection<DataBox> tasks);


    /**
     * Удаляет план обучения
     */
    void deletePlan(long id);


}
