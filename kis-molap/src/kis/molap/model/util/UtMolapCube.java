package kis.molap.model.util;

import jandcode.commons.*;
import jandcode.commons.datetime.*;
import jandcode.commons.variant.*;
import jandcode.core.store.*;
import kis.molap.model.coord.*;
import kis.molap.model.coord.impl.*;
import kis.molap.model.value.*;
import org.apache.commons.logging.*;

import java.util.*;

public class UtMolapCube {


    protected static Log log = LogFactory.getLog("Cube");

    public static void storeValuesToList(Store st, List<IVariantMap> res) {
        for (StoreRecord rec : st) {
            IVariantMap values = new VariantMapWrap(rec.getValues());
            res.add(values);
        }
    }

    /**
     * Записи lst типа "координата + dbeg + dend" переносит в список координат coordsRes.
     *
     * @param lst       Записи типа "координата + dbeg + dend"
     * @param cubeRange ограничение на диапазон dbeg .. dend
     * @param coordName имя другой координаты (кроме dbeg и dend)
     */
    public static void auditValuesToCoords_Period(List<IVariantMap> lst, Period cubeRange, String coordName, CoordList coordsRes) {
        for (IVariantMap rec : lst) {
            auditValueToCoord_Period(rec, cubeRange, coordName, coordsRes);
        }
    }

    public static void auditValuesToCoords_Period(Store st, Period cubeRange, String coordName, CoordList coordsRes) {
        List<IVariantMap> lst = new ArrayList<>();
        UtMolapCube.storeValuesToList(st, lst);
        UtMolapCube.auditValuesToCoords_Period(lst, cubeRange, coordName, coordsRes);
    }

    public static void auditValueToCoord_Period(IVariantMap rec, Period cubeRange, String coordName, CoordList coordsRes) {
        Period period = recToPeriod(rec);
        // Ограничение диапазона дат
        if (cubeRange.isCross(period)) {
            cubeRange.truncPeriod(period);
            //
            Object coordVal = rec.get(coordName);
            Coord coord = Coord.create();
            coord.put(coordName, coordVal);
            coord.put("dt", period);
            coordsRes.add(coord);
        } else {
            log.debug("Periods is not cross, audit: " + period + ", range: " + cubeRange);
        }
    }

    public static Period recToPeriod(IVariantMap rec) {
        XDateTime dbeg = rec.getDateTime("dbeg");
        XDateTime dend = rec.getDateTime("dend");
        // В журналах периодических таблиц допустимо появление dend==null.
        // Это бывает, когда запись добавляется, а dend не указывается в расчете
        // на более позднее замыкание диапазонов (см. kis.ntbd.utils.DbUtils.repairDendForIns}
        if (rec.isValueNull("dend")) {
            dend = UtDateTime.EMPTY_DATETIME_END;
        }

        Period period = new Period(dbeg.toDate(), dend.toDate());

        return period;
    }

    /**
     * Предполагается, что в store структура типа:
     * dbeg, dend и значения (нужные поля переданы в fieldNames через запятую).
     * Заполняем (протягиваем) значения из store в res, за каждый день с intervalDbeg по intervalDend
     */
    public void fillValues_disperseDay(Store store, String fieldNames, Coord coord, XDate intervalDbeg, XDate intervalDend, ICalcResultStream res) throws Exception {
        String[] fieldNamesArr = fieldNames.split(",");

        XDate dt_day = intervalDbeg;
        int store_pos = 0;
        while (dt_day.compareTo(intervalDend) <= 0) {
            // Двигаем позицию в store (store_pos) вперед, пока есть куда двигать,
            // а нужная нам дата дата (dt_day) заведомо мньше периода dbeg..dend из store.
            while (store_pos < (store.size() - 1) && dt_day.compareTo(store.get(store_pos).getDate("dend")) >= 0) {
                store_pos++;
            }

            // Нужная нам дата (dt_day) попадает в период dbeg..dend (из store)?
            boolean found = (store_pos < store.size() &&
                    dt_day.compareTo(store.get(store_pos).getDate("dbeg")) >= 0 &&
                    dt_day.compareTo(store.get(store_pos).getDate("dend")) < 0);
            //
            if (found) {
                // Создаем запись (values) с данными
                Coord coordRec = Coord.create();
                coordRec.putAll(coord.getValues());
                coordRec.put("dt", dt_day); // Этот метод именно по дням, поэтому имя поля явно будет "dt"
                //
                // Заполняем запись - перебираем поля из fieldNamesArr
                ValueSingle value = ValueSingle.create();
                for (String fieldName : fieldNamesArr) {
                    value.put(fieldName, store.get(store_pos).getValue(fieldName));
                }
                //
                CalcResult rec = new CalcResult();
                rec.coord = coord;
                rec.value = value;
                //
                res.addValue(rec);

            }

            // Переходим к следующей дате
            dt_day = dt_day.addDays(1);
        }
    }

    public static Collection<Object> coordsValueToList(Iterable<Coord> coords, String fieldName) {
        if (coords == null) {
            return null;
        }

        //
        Set<Object> res = new HashSet<>();

        for (Coord coord : coords) {
            res.add(UtCnv.toLong(coord.get(fieldName)));
        }

        return res;
    }

    public static Collection<Long> coordsValueToListLong(Iterable<Coord> coords, String fieldName) {
        if (coords == null) {
            return null;
        }

        //
        Set<Long> res = new HashSet<>();

        for (Coord coord : coords) {
            res.add(UtCnv.toLong(coord.get(fieldName)));
        }

        return res;
    }


}
