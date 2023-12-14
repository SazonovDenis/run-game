package kis.molap.model.cli;

import jandcode.commons.*;
import jandcode.commons.cli.*;
import jandcode.commons.conf.*;
import jandcode.core.cli.*;
import jandcode.core.dbm.*;
import jandcode.core.dbm.mdb.*;
import jandcode.core.dbm.sql.*;

import java.util.*;

public class DbInitAuditCmd extends BaseAppCliCmd {

    @Override
    public void exec() throws Exception {
        ModelService modelService = getApp().bean(ModelService.class);
        Model model = modelService.getModel();
        Mdb mdb = model.createMdb();

        //
        mdb.connect();
        try {
            Conf conf = model.getConf().getConf("ddl-script/cube-audit");
            SqlText sql = mdb.createSqlText(conf);
            mdb.execScript(sql);

            //
            List<String> sc = UtSql.splitScript(sql.getSql());
            System.out.println("Molap audit DDL done, items count: " + sc.size());
        } finally {
            mdb.disconnect();
        }
    }

    @Override
    public void cliConfigure(CliDef b) {
        b.desc("Создание аудита для кубов");
    }

}
