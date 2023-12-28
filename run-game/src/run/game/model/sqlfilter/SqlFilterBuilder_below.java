package run.game.model.sqlfilter;

import jandcode.core.apx.dbm.sqlfilter.*;

/**
 *
 */
public class SqlFilterBuilder_below implements SqlFilterBuilder {

    public void buildWhere(SqlFilterContext ctx) {
        String pname = ctx.paramName("value");
        Object v = ctx.getValue();
        //
        ctx.addWhere(ctx.getSqlField() + " < :" + pname);
        ctx.setParam(pname, v);
    }

}
