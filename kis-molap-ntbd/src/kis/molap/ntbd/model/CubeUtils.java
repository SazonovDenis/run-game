package kis.molap.ntbd.model;

import jandcode.core.store.*;
import kis.molap.model.value.*;

import java.util.*;

public class CubeUtils {

    public static double discardExtraDigits(double val) {
        double divider = 10E6;
        return Math.round(val * divider) / divider;
    }

    public static double discardExtraDigits(double val, int digits) {
        double divider = Math.pow(10, digits);
        return Math.round(val * divider) / divider;
    }

    public static void discardExtraDigits(ValueSingle value, int digits) {
        double divider = Math.pow(10, digits);
        for (String key : value.getValues().keySet()) {
            if (value.get(key) instanceof Double valDouble) {
                value.put(key, Math.round(valDouble * divider) / divider);
            }
        }
    }

    /**
     * Для каждого значения ключевого поля keyField собирает из store список записей.
     *
     * @param store    источник значений
     * @param keyField ключевое поле для группировки
     * @return Map<значение ключевого поля, список с собранными записями>
     */
    public static Map<Object, List<StoreRecord>> collectGroupBy_records(Store store, String keyField) {
        Map<Object, List<StoreRecord>> res = new HashMap<>();

        for (StoreRecord rec : store) {
            Object keyValue = rec.getValue(keyField);

            //
            List<StoreRecord> listRecords = res.get(keyValue);

            //
            if (listRecords == null) {
                listRecords = new ArrayList<>();
                res.put(keyValue, listRecords);
            }

            //
            listRecords.add(rec);
        }

        return res;
    }
}
