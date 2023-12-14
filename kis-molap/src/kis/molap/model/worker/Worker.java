package kis.molap.model.worker;

import jandcode.commons.datetime.*;
import jandcode.core.store.*;

/**
 * Умеет выполнять расчет кубов, учитывает зависимости, обеспечивает запись результата и журналов, устанавливает возраста кубов.
 * Знает, в каком порядке вызывать методы кубов, пространств и утилитных классов.
 */
public interface Worker {


    /**
     * @return Информация о состоянии кубов
     */
    Store loadInfo(String cubeName) throws Exception;


    /**
     * @return Информация о состоянии аудита
     */
    Store loadInfoAudit() throws Exception;


    /**
     * Отметить возраст обработанного аудита всех кубов
     *
     * @param auditAgeTo новый возраст обработанного аудита
     */
    void markAuditCubeAll(long auditAgeTo) throws Exception;


    /**
     * Отметить возраст обработанного аудита
     *
     * @param cubeName   куб
     * @param auditAgeTo новый возраст обработанного аудита
     */
    void markAuditCube(String cubeName, long auditAgeTo) throws Exception;


    /**
     * Заполнение куба (не аудит) за интервал от intervalDbeg до intervalDend (включительно)
     * <p>
     * Обновляет границы (максимальный размер) куба после расчета.
     *
     * @param cubeName     куб
     * @param intervalDbeg первая дата интервала
     * @param intervalDend последняя интервала
     * @param calcCascade  нужно ли предварительно расчитать все влияющие кубы за тот же период
     */
    void calcIntervalCube(String cubeName, XDate intervalDbeg, XDate intervalDend, boolean calcCascade) throws Exception;


    /**
     * Заполнение всех кубов за интервал от intervalDbeg до intervalDend (включительно)
     * <p>
     * Обеспечивает порядок расчета, учитывает включен куб или нет.
     * Кля каждого куба гарантирует, что заполняется не только куб, но и все его влияющие (даже если влияющий отключен).
     * <p>
     * Обновляет границы (максимальный размер) кубов после расчета.
     *
     * @param intervalDbeg первая дата интервала
     * @param intervalDend последняя интервала
     */
    void calcIntervalAll(XDate intervalDbeg, XDate intervalDend) throws Exception;


    /**
     * Расчитывает изменения по аудиту (все изменения во влияющих таблицах) для куба cubeName.
     * Учитывает изменения, сделанные от возраста auditAgeFrom до возраста auditAgeTo.
     * <p>
     * Расчитывается только указанный куб, без всех его влияющиих.
     * <p>
     * Обновляет возраст куба после расчета.
     *
     * @param cubeName     куб
     * @param auditAgeFrom возраст аудита, начало
     * @param auditAgeTo   возраст аудита, окончание
     * @param calcCascade  нужно ли предварительно учесть изменения для влияющих кубов к тому же моменту
     */
    void calcAuditCube(String cubeName, long auditAgeFrom, long auditAgeTo, boolean calcCascade) throws Exception;


    /**
     * Расчитывает изменения по аудиту (все изменения во влияющих таблицах) для всех кубов.
     * Учитывает изменения, сделанные от возраста auditAgeFrom до возраста auditAgeTo.
     * <p>
     * Обеспечивает порядок кубов, учитывает включен куб или нет.
     * Кля каждого куба гарантирует, что расчитывается не только куб, но и все его влияющие (даже если влияющий отключен).
     * <p>
     * Обновляет возраста кубов после расчета.
     */
    void calcAuditAll(long auditAgeFrom, long auditAgeTo) throws Exception;


}
