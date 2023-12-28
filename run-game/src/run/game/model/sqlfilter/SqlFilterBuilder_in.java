package run.game.model.sqlfilter;

import jandcode.commons.*;
import jandcode.core.apx.dbm.sqlfilter.*;
import run.game.util.*;

import java.util.*;

/**
 *
 */
public class SqlFilterBuilder_in implements SqlFilterBuilder {

    public void buildWhere(SqlFilterContext ctx) {
        Object paramValue = ctx.getValue();

        //
        List<Long> ids = DataUtils.toListLong(paramValue);
        String idsStr = UtString.join(ids, ",");

        // Параметр filterNull = true, если нужно увидеть null значения
        // Полезно при поиске скважин, не привязанных к организации
        boolean filterNull = (boolean) ctx.getAttrs().getOrDefault("filterNull", true);

        //
        if (filterNull) {
            // Tab.org in (1,2)
            ctx.addWhere(ctx.getSqlField() + " in (" + idsStr + ")");
        } else {
            // (Tab.org in (1,2) or Tab.org is null)
            ctx.addWhere("(" + ctx.getSqlField() + " in (" + idsStr + ") or " + ctx.getSqlField() + " is null)");
        }
    }

}
