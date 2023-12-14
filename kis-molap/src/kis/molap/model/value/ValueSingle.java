package kis.molap.model.value;

import kis.molap.model.value.impl.*;

import java.util.*;

/**
 * Значение координаты. Одиночное значение (для одной координаты).
 * По структуре - Map, где ключи - поля куба, а значения - значения полей.
 * <p>
 * Представляет некоторые поля одной записи в пространстве куба.
 */
public interface ValueSingle extends Value {

    static ValueSingle create() {
        return new ValueSingleImpl();
    }

    /**
     * Значение поля key
     *
     * @param key Имя поля
     * @return Значение поля
     */
    Object get(String key);

    /**
     * Значение поля key, приведенное к double
     *
     * @param key Имя поля
     * @return Значение поля
     */
    double getDouble(String key);

    boolean containsKey(String key);

    /**
     * Задать значение поля key
     *
     * @param key   Имя поля
     * @param value Значение поля
     */
    void put(String key, Object value);

    void putAll(Map<String, Object> values);

    Map<String, Object> getValues();

}
