package kis.molap.model.coord.impl;

import java.util.*;

public class PeriodIterable implements Iterable<Period> {

    Object value;

    public PeriodIterable(Object valueDate) {
        this.value = valueDate;
    }

    @Override
    public Iterator<Period> iterator() {
        return new PeriodIterator(value);
    }

}
