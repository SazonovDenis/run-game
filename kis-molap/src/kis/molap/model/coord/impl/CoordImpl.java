package kis.molap.model.coord.impl;

import jandcode.commons.collect.*;
import jandcode.commons.datetime.*;
import kis.molap.model.coord.*;

import java.util.*;


public class CoordImpl implements Coord {


    // ---
    // ICubeCoord
    // ---

    private Map<String, Object> map = new HashMapNoCase<>();

    public Object get(String key) {
        return map.get(key);
    }

    public void put(String key, Object value) {
        map.put(key, value);
    }

    public void putAll(Map<String, Object> values) {
        map.putAll(values);
    }

    public Map<String, Object> getValues() {
        return map;
    }


    /**
     * Сравнение по значению координат.
     *
     * @return true, если значения всех координат совпадают, кроме координат типа "дата".
     * Что считать датой - определяет метод {@link CoordImpl#isDateCoord}
     */
    public boolean equals(Object other) {
        if (other instanceof Coord) {

            //
            Map<String, Object> coordsValuesOther = ((Coord) other).getValues();

            // раньше проверялось только на: coordsValuesOther.size() >= map.size()
            if (coordsValuesOther.size() != map.size()) return false;

            //
            for (Map.Entry<String, Object> e : coordsValuesOther.entrySet()) {
                Object valueOther = e.getValue();
                Object valueThis = get(e.getKey());

                // Для сведения
                if (valueOther == null) {
                    System.err.println("Значение координаты == null, key = " + e.getKey());
                }
                if (valueThis == null) {
                    System.err.println("Значение координаты == null, key = " + e.getKey());
                }

                //
                if (isDateCoord(valueThis) || isDateCoord(valueOther)) {
                    // Значения типа "дата" не участвуют при сравнении (игнорируем)
                    continue;
                }

                if (valueOther == null && valueThis == null) {
                    // обе координаты равны null - считаем равными
                    continue;
                }

                if (valueOther == null || valueThis == null) {
                    // одна из координат null - считаем координаты не совпадающими - больше не сравниваем
                    return false;
                }

                if (!valueOther.equals(valueThis)) {
                    // найдена не совпадающая координа - больше не сравниваем
                    return false;
                }
            }

            //
            return true;
        } else {
            return super.equals(other);
        }
    }


    /**
     * Вычисление hash по значению координат.
     *
     * @return hash по значениям всех координат, кроме координат типа "дата".
     * Что считать датой - определяет метод {@link CoordImpl#isDateCoord}
     */
    public int hashCode() {
        int h = 0;
        for (Object value : map.values()) {
            if (isDateCoord(value)) {
                // Значения типа "Дата" не участвуют в расчете hashCode (игнорируем)
                h = h + 1;
            } else {
                h = h + value.hashCode();
            }
        }
        return h;
    }


    // ---
    // Утилитные методы
    // ---

    /**
     * @return Является ли значение координаты датой, например, MoPeriod или MoPeriodList
     */
    public static boolean isDateCoord(Object value) {
        return value instanceof XDate || value instanceof Period || value instanceof PeriodList;
    }

    /**
     * Для печати в консоли
     */
    public String toString() {
        return map.toString();
    }


}
