package kis.molap.model.coord.impl;

import jandcode.commons.datetime.*;

import java.util.*;

/**
 * Умеет перебрать значение координаты как набор {@link Period}
 */
public class PeriodIterator implements Iterator<Period> {

    int idx;
    Period valuePeriod;
    PeriodList valuePeriodList;

    public PeriodIterator(Object valueDate) {
        if (valueDate instanceof XDate valueXDate) {
            this.valuePeriod = new Period(valueXDate, valueXDate);
        } else if (valueDate instanceof Period valuePeriod) {
            this.valuePeriod = valuePeriod;
        } else {
            this.valuePeriodList = (PeriodList) valueDate;
        }
        idx = 0;
    }

    @Override
    public boolean hasNext() {
        if (valuePeriod != null) {
            return idx <= 0;
        } else {
            return idx < valuePeriodList.size();
        }
    }

    @Override
    public Period next() {

        if (!hasNext()) {
            return null;
        }

        //
        Period value;
        if (valuePeriod != null) {
            value = valuePeriod;
        } else {
            value = valuePeriodList.get(idx);
        }
        idx = idx + 1;

        //
        return value;
    }

}
