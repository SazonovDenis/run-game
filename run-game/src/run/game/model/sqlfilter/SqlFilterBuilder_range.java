package run.game.model.sqlfilter;

import jandcode.commons.*;
import jandcode.core.apx.dbm.sqlfilter.*;

import java.util.*;

/**
 *
 */
public class SqlFilterBuilder_range implements SqlFilterBuilder {

    public void buildWhere(SqlFilterContext ctx) {
        String paramName = ctx.paramName("value");
        List<String> paramValues = UtCnv.toList(ctx.getValue(), " ");

        //
        if (paramValues.size() == 1 && !UtCnv.isEmpty(paramValues.get(0))) {
            String sign = getSign(paramValues.get(0));
            String paramValue = getValue(paramValues.get(0));
            ctx.addWhere(ctx.getSqlField() + " " + sign + " :" + paramName);
            ctx.setParam(paramName, paramValue);
        }

        //
        if (paramValues.size() == 2 && !UtCnv.isEmpty(paramValues.get(1))) {
            ctx.addWhere(ctx.getSqlField() + " >= :" + paramName + "_min");
            ctx.setParam(paramName + "_min", paramValues.get(0));
            ctx.addWhere(ctx.getSqlField() + " <= :" + paramName + "_max");
            ctx.setParam(paramName + "_max", paramValues.get(1));
        }
    }

    private String getValue(String paramValue) {
        paramValue = paramValue.trim();
        if (paramValue.startsWith("<") || paramValue.startsWith(">")) {
            return paramValue.substring(1);
        }
        if (paramValue.startsWith("<=") || paramValue.startsWith(">=")) {
            return paramValue.substring(2);
        }
        return paramValue;
    }

    private String getSign(String paramValue) {
        paramValue = paramValue.trim();
        if (paramValue.startsWith("<")) {
            return "<";
        }
        if (paramValue.startsWith(">")) {
            return ">";
        }
        if (paramValue.startsWith("<=")) {
            return "<=";
        }
        if (paramValue.startsWith(">=")) {
            return ">=";
        }
        return ">=";
    }

}
