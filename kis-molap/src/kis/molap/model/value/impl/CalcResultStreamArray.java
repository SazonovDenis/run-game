package kis.molap.model.value.impl;

import kis.molap.model.value.*;

import java.util.*;

public class CalcResultStreamArray extends ArrayList<CalcResult> implements ICalcResultStream {

    @Override
    public void addValue(CalcResult value) {
        add(value);
    }

}
