package kis.molap.model.coord;

import jandcode.commons.datetime.*;
import jandcode.commons.error.*;
import kis.molap.model.coord.impl.*;

import java.util.*;

public class UtCoord {


    /**
     * Количество точек (с учетом списка периодов и размеров каждого периода)
     * В отличие от size() для координаты типа "период" возвращает количество отдельных дат в этом периоде.
     */
    public static int sizeFull(CoordList coords) {
        int size = 0;

        //
        for (Coord coord : coords) {
            Object valueDate = getValueDate(coord);

            if (valueDate instanceof XDate) {
                // Коррдината "дата" - одиночная дата
                size = size + 1;
            } else if (valueDate instanceof Period valuePeriod) {
                // Коррдината "дата" - период
                size = size + valuePeriod.dend.diffDays(valuePeriod.dbeg) + 1;
            } else if (valueDate instanceof PeriodList valuePeriodList) {
                // Коррдината "дата" - список периодов
                for (Period period : valuePeriodList) {
                    size = size + period.dend.diffDays(period.dbeg) + 1;
                }
            }
        }

        //
        return size;
    }


    public static String getValueDateCoordName(Coord coord) {
        for (Map.Entry e : coord.getValues().entrySet()) {
            Object value = e.getValue();
            if (value instanceof XDate || value instanceof Period || value instanceof PeriodList) {
                return (String) e.getKey();
            }
        }
        return null;
    }

    public static Object getValueDate(Coord coord) {
        for (Map.Entry e : coord.getValues().entrySet()) {
            Object value = e.getValue();
            if (value instanceof XDate || value instanceof Period || value instanceof PeriodList) {
                return value;
            }
        }
        return null;
    }

    public static Iterable<Period> getValueDateIterator(Coord coord) {
        for (Map.Entry e : coord.getValues().entrySet()) {
            Object value = e.getValue();
            if (value instanceof XDate || value instanceof Period || value instanceof PeriodList) {
                return new PeriodIterable(value);
            }
        }
        throw new XError("Coord has no date value, coord: " + coord);
    }

}
