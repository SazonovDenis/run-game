package kis.molap.model.coord;

import kis.molap.model.coord.impl.*;

import java.util.*;

/**
 * Список координат {@link Coord}.
 * <p>
 * Координаты в списке уникальны по всем значениям, кроме коордитат типа {@link Period},
 * которые не учитываются в определении уникальности.
 * <p>
 * Уникальность гарантируется методами в классе {@link CoordImpl}
 */
public interface CoordList extends Iterable<Coord> {

    static CoordList create() {
        return new CoordListImpl();
    }

    int size();

    void add(Coord cubecoord);

    void addAll(CoordList coords);

    Coord get(Coord cubecoord);

    List<Coord> toList();

}