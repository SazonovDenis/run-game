package kis.molap.model.worker;

import jandcode.commons.datetime.*;
import jandcode.core.dbm.mdb.*;
import kis.molap.model.conf.*;
import kis.molap.model.coord.impl.*;


/**
 * Обеспечивает чтение и запись отметок в БД:
 * - возраста аудита
 * - границ кубов.
 */
public interface AgeManager extends IMdbLinkSet {


    /**
     * @return Общий возраст аудита БД. До какого возраста есть журнал аудита.
     */
    long getActualAge() throws Exception;


    /**
     * Возраст актуальности куба.
     * Все изменения, совершенные до этого возраста, уже учтены (т.е. расчитаны и записаны).
     */
    long getCubeAgeDone(CubeInfo cubeInfo) throws Exception;

    /**
     * Установить возраст актуальности куба.
     */
    void setCubeAgeDone(CubeInfo cubeInfo, long age) throws Exception;


    /**
     * @return Значение минимальной и максимальной координаты "Дата", до которой расширен куб.
     */
    Period getMinMaxDt(CubeInfo cubeInfo) throws Exception;

    /**
     * Установить максимальную границу, до которой расширен куб.
     */
    void setMaxDt(CubeInfo cubeInfo, XDate dtExpand) throws Exception;

    /**
     * Установить минимальную границу, до которой расширен куб.
     */
    void setMinDt(CubeInfo cubeInfo, XDate dtExpand) throws Exception;


}
