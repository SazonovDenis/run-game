package kis.molap.model.coord.impl;

import jandcode.commons.datetime.*;
import jandcode.commons.error.*;

/**
 * Тип данных - период.
 * Для значений координат "интервал дат".
 */
public class Period {

    public XDate dbeg;
    public XDate dend;

    public Period(XDate dbeg, XDate dend) {
        this.dbeg = dbeg;
        this.dend = dend;
        validatePeriod(this);
    }

    @Override
    public Period clone() {
        return new Period(this.dbeg, this.dend);
    }

    @Override
    public String toString() {
        return dbeg.toString() + ".." + dend.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Period otherPeriod) {
            return this.dbeg.equals(otherPeriod.dbeg) && this.dend.equals(otherPeriod.dend);
        } else {
            return false;
        }
    }

    /**
     * @return Есть ли общие дни у периодов
     */
    public boolean isCross(Period period) {
        return (this.dbeg.compareTo(period.dend) <= 0 && this.dend.compareTo(period.dbeg) >= 0);
    }

    /**
     * Обрезать период по ограничению.
     * Ошибка, если получается пустой диапазон.
     *
     * @param period ограничиваемый период
     */
    public void truncPeriod(Period period) {
        if (period.dbeg.compareTo(this.dbeg) < 0) {
            period.dbeg = this.dbeg;
        }
        if (period.dend.compareTo(this.dend) > 0) {
            period.dend = this.dend;
        }
        validatePeriod(period);
    }

    /**
     * Получить пересечение периодов this и period.
     * Ошибка, если получается пустой диапазон.
     *
     * @return пересечение преиодов
     */
    public Period getCross(Period period) {
        Period res = new Period(this.dbeg, this.dend);

        if (period.dbeg.compareTo(res.dbeg) > 0) {
            res.dbeg = period.dbeg;
        }

        if (period.dend.compareTo(res.dend) < 0) {
            res.dend = period.dend;
        }

        validatePeriod(res);

        return res;
    }

    /**
     * @return Находится ли дата dt внутри периода
     */
    public boolean isInside(XDate dt) {
        return (this.dbeg.compareTo(dt) <= 0 && this.dend.compareTo(dt) >= 0);
    }

    /**
     * @return Находится ли период period внутри периода
     */
    public boolean isInside(Period period) {
        return (this.dbeg.compareTo(period.dbeg) <= 0 && this.dend.compareTo(period.dend) >= 0);
    }

    /**
     * @return Можно ли слить два интервала в один. Если есть пересечение или периоды примыкают друг к другу
     */
    public boolean isMergeable(Period period) {
        return (this.dbeg.compareTo(period.dend.addDays(1)) <= 0 && this.dend.compareTo(period.dbeg.addDays(-1)) >= 0);
    }

    /**
     * Слить два интервала в один.
     *
     * @return Объединенный интервал
     */
    public Period getMerge(Period value) {
        if (!isMergeable(value)) {
            throw new XError("Periods is not cross, this: " + this + ", new: " + value);
        }

        XDate dbeg;
        XDate dend;
        if (this.dbeg.compareTo(value.dbeg) < 0) {
            dbeg = this.dbeg;
        } else {
            dbeg = value.dbeg;
        }
        if (this.dend.compareTo(value.dend) > 0) {
            dend = this.dend;
        } else {
            dend = value.dend;
        }

        return new Period(dbeg, dend);
    }

    private static void validatePeriod(Period period) {
        if (period.dend.compareTo(period.dbeg) < 0) {
            throw new XError("validatePeriod: dend < dbeg, period: " + period);
        }
    }

}


