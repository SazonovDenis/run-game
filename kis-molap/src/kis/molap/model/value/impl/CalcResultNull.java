package kis.molap.model.value.impl;

import kis.molap.model.value.*;

/**
 * Приемник результата с ограниченной емкостью, остальное в пустоту.
 * Для отладки.
 */
public class CalcResultNull extends CalcResultStreamArray {

    int size = 0;
    int sizeMax = 100;

    @Override
    public int size() {
        return size;
    }

    @Override
    public void addValue(CalcResult value) {
        size = size + 1;
        if (size < sizeMax) {
            super.addValue(value);
        }
    }

    @Override
    public CalcResult get(int index) {
        if (index < sizeMax) {
            return super.get(index);
        } else {
            return null;
        }
    }


}
