package kis.molap.model.coord.impl;

import jandcode.commons.error.*;

import java.util.*;

/**
 * Тип данных - набор периодов.
 * Используется как одно из возможных значений координаты.
 */
public class PeriodList extends ArrayList<Period> {

    public boolean add(Period period) {
        // Такая ситуация вредит во многих местах и у нее нет полезных применений,
        // поэтому будем явно выкидывать ошибку.
        if (period.dbeg.compareTo(period.dend) > 0) {
            throw new XError("moPeriod.dbeg > moPeriod.dend");
        }
        //
        return super.add(period.clone());
    }

}
