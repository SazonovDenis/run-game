package run.game.util;

import jandcode.commons.*;
import jandcode.core.store.*;

import java.util.*;

/**
 * Утилиты для работы с данными
 */
public class DataUtils {

    public static List<Long> toListLong(Object val) {
        List<String> vals;
        if (val instanceof Long || val instanceof Integer) {
            vals = new ArrayList<>();
            vals.add(val.toString());
        } else {
            vals = UtCnv.toList(val);
        }

        List<Long> res = new ArrayList<>();
        for (String v : vals) {
            res.add(UtCnv.toLong(v));
        }

        return res;
    }

    /**
     * Превращает Store в List<Map>.
     * Метод полезен в тестах и других ситуациях,
     * где нужно вызвать dao-метод,
     * который принимает List<Map> вмеcто Store.
     */
    public static List<Map> storeToList(Store st) {
        List<Map> res = new ArrayList<>();

        for (StoreRecord rec : st) {
            res.add(rec.getValues());
        }

        return res;
    }

}
