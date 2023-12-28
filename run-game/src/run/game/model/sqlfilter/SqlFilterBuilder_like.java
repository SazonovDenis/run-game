package run.game.model.sqlfilter;

import jandcode.core.apx.dbm.sqlfilter.*;

/**
 *
 */
public class SqlFilterBuilder_like implements SqlFilterBuilder {

    public void buildWhere(SqlFilterContext ctx) {
        String paramName = ctx.paramName("value");
        String paramValue = String.valueOf(ctx.getValue());

        //
        ctx.addWhere("upper(" + ctx.getSqlField() + ") like :" + paramName);

        //
        paramValue = "%" + paramValue.toUpperCase() + "%";
        ctx.setParam(paramName, paramValue);
    }

}
