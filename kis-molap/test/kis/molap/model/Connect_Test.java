package kis.molap.model;

import jandcode.commons.*;
import jandcode.core.db.*;
import jandcode.core.dbm.*;
import jandcode.core.dbm.mdb.*;
import jandcode.core.dbm.mdb.impl.*;
import jandcode.core.dbm.test.*;
import jandcode.core.store.*;
import org.junit.jupiter.api.*;

public class Connect_Test extends Dbm_Test {

    @Test
    public void test_connect() throws Exception {
        ModelService svc = app.bean(ModelService.class);
        Model modelRead = svc.getModel("default");
        Model modelWrite = svc.getModel("write");
        Db dbRead = modelRead.createDb();
        Db dbWrite = modelWrite.createDb();
        Mdb mdbRead = new MdbImpl(modelRead, dbRead);
        Mdb mdbWrite = new MdbImpl(modelWrite, dbWrite);

        //
        System.out.println("dbRead: " + dbRead.getDbSource().getProps());
        System.out.println("dbWrite: " + dbWrite.getDbSource().getProps());

        //
        try {
            dbRead.connect();
            System.out.println("dbRead.connect - ok");
        } catch (Exception e) {
            System.out.println("dbRead.connect - error: " + e.getMessage());
        }
        try {
            dbWrite.connect();
            System.out.println("dbWrite.connect - ok");
        } catch (Exception e) {
            System.out.println("dbWrite.connect - error: " + e.getMessage());
        }

        //
        Store tab_default = mdbRead.loadQuery("select * from Molap_statistic");
        Store tab_write = mdbWrite.loadQuery("select * from Cube_WellDt");

        //
        utils.outTable(tab_write, 10);
        utils.outTable(tab_default, 10);
    }


    @Test
    public void test_several_opened_query() throws Exception {
        dbm.getMdb().outTable(dbm.getMdb().loadQuery("select * from well order by id limit 10"));

        //
        DbQuery q1 = dbm.getMdb().createQuery("select * from well order by id");
        DbQuery q2 = dbm.getMdb().createQuery("select * from wellStatus order by well");
        DbQuery q3 = dbm.getMdb().createQuery("update well set uwi = :uwi where id = :id");
        q1.open();
        q2.open();
        for (int i = 1; i <= 10; i++) {
            System.out.println("rec: " + i);
            System.out.println("id: " + q1.getString("id") + ", well.uwi: " + q1.getString("uwi"));
            System.out.println("id: " + q2.getString("id") + ", wellStatus.dbeg: " + q2.getString("dbeg"));

            q3.setParams(UtCnv.toMap("id", q1.getString("id"), "uwi", q1.getString("uwi") + "_"));
            q3.exec();

            q1.next();
            q2.next();
        }
        q1.close();
        q2.close();

        //
        dbm.getMdb().outTable(dbm.getMdb().loadQuery("select * from well order by id limit 10"));
    }

}
