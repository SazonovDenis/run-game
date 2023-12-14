package kis.molap.model.value.impl;

import kis.molap.model.value.*;

import java.util.*;


public class ValueListImpl implements ValueList {

    protected List<ValueSingle> values = new ArrayList<>();

    @Override
    public void add(ValueSingle value) {
        values.add(value);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public ValueSingle get(int i) {
        return values.get(i);
    }

    public String toString() {
        if (size() == 0) {
            return "[<empty>]";
        } else if (size() == 1) {
            return "[" + values.get(0) + "]";
        } else {
            return "[" + values.get(0) + ", ...], size: " + size();
        }

    }
}
