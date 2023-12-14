package kis.molap.ntbd.model;

import jandcode.commons.*;

import java.util.*;

// Дубликат kis.ntbd.utils.DataUtils.toListLong,
// сделанный ради того, чтобы не тянуть зависимости к kis.ntbd.*
public class CubeDataUtils {

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
