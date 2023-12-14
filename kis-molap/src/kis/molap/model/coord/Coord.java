package kis.molap.model.coord;

import kis.molap.model.coord.impl.*;

import java.util.*;

/**
 * Координата. Точка в координатном пространстве куба.
 * <p>
 * По структуре - Map, где ключи - координаты куба, а значения - значения координат
 */
public interface Coord {

    static Coord create() {
        return new CoordImpl();
    }

    Object get(String key);

    void put(String key, Object value);

    void putAll(Map<String, Object> values);

    Map<String, Object> getValues();

}
