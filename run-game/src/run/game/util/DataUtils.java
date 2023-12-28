package run.game.util;

import jandcode.commons.*;

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

}
