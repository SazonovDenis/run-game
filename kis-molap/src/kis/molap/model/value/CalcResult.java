package kis.molap.model.value;

import kis.molap.model.coord.*;

/**
 * Результат расчета. Пара: координата + значения.
 */
public class CalcResult {

    /**
     * Координата
     */
    public Coord coord = null;

    /**
     * Значение (единственное {@link ValueSingle} или несколько {@link ValueList})
     */
    public Value value = null;

    @Override
    public String toString() {
        return "coord: " + coord.toString() + ", value: " + value.toString();
    }

}
