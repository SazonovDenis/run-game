package kis.molap.model.value.impl;

import jandcode.commons.*;
import jandcode.commons.collect.*;
import kis.molap.model.value.*;

import java.util.*;


public class ValueSingleImpl implements ValueSingle {

    Map<String, Object> values = new HashMapNoCase<>();

    public Object get(String key) {
        key = key.toLowerCase();
        return values.get(key);
    }

    public double getDouble(String key) {
        return UtCnv.toDouble(get(key));
    }

    public boolean containsKey(String key) {
        return values.containsKey(key);
    }

    public void put(String key, Object value) {
        key = key.toLowerCase().intern();
        values.put(key, value);
    }

    public void putAll(Map<String, Object> values) {
        this.values.putAll(values);
    }

    public Map<String, Object> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return values.toString();
    }


}
