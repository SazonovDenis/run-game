package kis.molap.model.coord.impl;

import jandcode.commons.datetime.*;
import jandcode.commons.error.*;
import kis.molap.model.coord.*;

import java.util.*;

/**
 * Список координат ICubeCoord. Координаты в списке уникальны по всем значениям,
 * кроме коордитат типа "дата", которые не учитываются в определении уникальности.
 * Уникальность гарантируется методами в классе {@link CoordImpl}
 */
public class CoordListImpl implements CoordList {

    private Map<Coord, Coord> map = new HashMap<>();

    public int size() {
        // Делегируем
        return map.size();
    }

    public void addAll(CoordList coords) {
        // Реализовываем
        for (Coord coord : coords) {
            map.put(coord, coord);
        }
    }

    public Coord get(Coord coord) {
        // Делегируем
        return map.get(coord);
    }

    public Iterator<Coord> iterator() {
        // Делегируем
        return map.keySet().iterator();
    }

    public List<Coord> toList() {
        // Делегируем
        return map.keySet().stream().toList();
    }

    /**
     * Как обычное добавление, но обеспечивает "схлопывание" пересекающихся периодов в один.
     */
    public void add(Coord coord) {
        // Реализовываем

        // Координата coord новая?
        // Учтем, что значения типа MoPeriodList не участвуют в расчете kis.molap.model.Cube.impl.CubeCoord.hashCode
        Coord coord_existing = get(coord);
        if (coord_existing == null) {
            // Координата coord совсем новая - добавим ее
            map.put(coord, coord);
        } else {
            // Координата coord уже была - НЕ добавляем,
            // а просто добавим ее значение "даты" в ранее накопленные периоды (расширим существующие).

            // Значение "дата" добавляемой координаты
            Object valueDate_new = UtCoord.getValueDate(coord);

            // У добавляемой координаты нет значения "дата" - объединять нечего, мы просто добавляем существующую координату
            if (valueDate_new == null) {
                return;
            }

            // Значение "дата" ранее добавленной координаты
            Object valueDate_existing = UtCoord.getValueDate(coord_existing);
            String valueDateCoordName = UtCoord.getValueDateCoordName(coord_existing);

            // Защита от дурака: у ранее добавленной координаты coord_existing должно быть значение типа "дата",
            // иначе зачем мы ранее добавили координату БЕЗ даты, а теперь добавлям С датой, какой в этом смысл?
            if (valueDate_existing == null) {
                throw new XError("existing valueDate == null, coord: " + coord_existing);
            }

            // Сливаем новое значение с ранее добавленными
            Object valueDateMerged = mergeValueDate(valueDate_new, valueDate_existing);
            coord.put(valueDateCoordName, valueDateMerged);
            map.put(coord, coord);
        }
    }


    /**
     * @return Сливаем значение value_new с ранее добавленными value_existing
     */
    private Object mergeValueDate(Object value_new, Object value_existing) {
        // Добавляем простую дату, которая совпадает с той, что уже есть
        if (value_new instanceof XDate && value_existing instanceof XDate && value_new.equals(value_existing)) {
            return value_existing;
        }

        // Добавляем простую дату, которая находится внутри того MoPeriod, что уже есть
        if (value_new instanceof XDate valueNew && value_existing instanceof Period valueExisting && valueExisting.isInside(valueNew)) {
            return value_existing;
        }

        // Добавляем MoPeriod, внутри которого находится простая дата, что уже есть
        if (value_new instanceof Period valueNew && value_existing instanceof XDate valueExisting && valueNew.isInside(valueExisting)) {
            return value_new;
        }

        // Добавляем MoPeriod, который совпадает с тем MoPeriod, что уже есть
        if (value_new instanceof Period && value_existing instanceof Period && value_new.equals(value_existing)) {
            return value_existing;
        }

        // Добавляем MoPeriod, который пересекается с тем MoPeriod, что уже есть
        if (value_new instanceof Period valueNew && value_existing instanceof Period valueExisting && valueNew.isMergeable(valueExisting)) {
            return valueNew.getMerge(valueExisting);
        }


        // Добавляем значение, которое не слилось существующими значениями.
        // Новое значение координаты теперь будет типа PeriodList, сливаем в него
        PeriodList periodList_existing;
        if (value_existing instanceof PeriodList) {
            // Существующее значение координаты - MoPeriodList
            periodList_existing = (PeriodList) value_existing;
        } else {
            Period period;
            if (value_existing instanceof Period) {
                // Существующее значение координаты - MoPeriod
                period = (Period) value_existing;
            } else {
                // Существующее значение координаты - XDate
                XDate valueDate = (XDate) value_existing;
                period = new Period(valueDate, valueDate);
            }

            periodList_existing = new PeriodList();
            periodList_existing.add(period);
        }

        PeriodList periodList_new;
        if (value_new instanceof PeriodList) {
            // Новое значение координаты - MoPeriodList
            periodList_new = (PeriodList) value_new;
        } else {
            Period period;
            if (value_new instanceof Period) {
                // Новое значение координаты - MoPeriod
                period = (Period) value_new;
            } else {
                // Новое значение координаты - XDate
                XDate valueDate = (XDate) value_new;
                period = new Period(valueDate, valueDate);
            }
            periodList_new = new PeriodList();
            periodList_new.add(period);
        }


        // Вновь добавленный moPeriodList_new схлопывается с уже существующими периодами moPeriodList_existing
        mergePeriodList(periodList_existing, periodList_new);


        //
        return periodList_existing;
    }


    private void mergePeriodList(PeriodList periodList_existing, PeriodList periodList_new) {
        for (Period period_new : periodList_new) {
            // Удалим все старые периоды, пересекающиеся с period_new
            for (int i = periodList_existing.size() - 1; i >= 0; i--) {
                Period period_existing = periodList_existing.get(i);
                // Периоды period_existing и period_new пересекаются?
                if (period_existing.isMergeable(period_new)) {
                    // Сливаем периоды в период period_new
                    period_new = period_existing.getMerge(period_new);
                    // Удаляем тот период, который period_new заменит
                    periodList_existing.remove(i);
                }

            }
            // Добавим period_new (возможно, он расширился)
            periodList_existing.add(period_new);
        }

    }


}