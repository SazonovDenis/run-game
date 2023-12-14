package kis.molap.model.worker.impl;

import jandcode.commons.*;
import jandcode.commons.datetime.*;
import jandcode.core.dbm.mdb.*;
import jandcode.core.store.*;
import kis.molap.model.conf.*;
import kis.molap.model.coord.impl.*;
import kis.molap.model.worker.*;
import org.apache.commons.logging.*;

public class AgeManagerImpl implements IMdbLinkSet, AgeManager {


    protected Mdb mdb;

    protected static Log log = LogFactory.getLog("AgeManager");

    // ---
    // IMdbLinkSet
    // ---

    @Override
    public void setMdb(Mdb mdb) {
        this.mdb = mdb;
    }


    // ---
    // IAgeManager
    // ---

    @Override
    public long getActualAge() throws Exception {
        long auditAge = mdb.loadQueryRecord("select max(id) id from Molap_audit").getLong("id");
        return auditAge;
    }


    @Override
    public long getCubeAgeDone(CubeInfo cubeInfo) throws Exception {
        StoreRecord rec = getRec(cubeInfo);
        long auditAgeDone = rec.getLong("done_id");
        return auditAgeDone;
    }


    @Override
    public void setCubeAgeDone(CubeInfo cubeInfo, long age) throws Exception {
        // Узнаем дату, которая соответствует возрасту age
        XDateTime done_dt = null;
        StoreRecord recAudit = mdb.loadQueryRecord("select dt from Molap_audit where id = " + age, false);
        if (recAudit != null) {
            done_dt = recAudit.getDateTime("dt");
        }
        //
        log.info("setCubeAgeDone, cube: " + cubeInfo.getName() + ", done_id: " + age + ", done_dt: " + done_dt);
        //
        StoreRecord rec = getRec(cubeInfo);
        mdb.updateRec("Molap_status", UtCnv.toMap("id", rec.getLong("id"), "done_id", age, "done_dt", done_dt));
    }


    @Override
    public Period getMinMaxDt(CubeInfo cubeInfo) throws Exception {
        StoreRecord rec = getRec(cubeInfo);
        Period cubeRange = new Period(rec.getDate("dt_min"), rec.getDate("dt_max"));
        return cubeRange;
    }

    @Override
    public void setMinDt(CubeInfo cubeInfo, XDate dt) throws Exception {
        log.info("setMinDt, cube: " + cubeInfo.getName() + ", dt_min: " + dt);
        //
        StoreRecord rec = getRec(cubeInfo);
        mdb.updateRec("Molap_status", UtCnv.toMap("id", rec.getLong("id"), "dt_min", dt));
    }

    @Override
    public void setMaxDt(CubeInfo cubeInfo, XDate dt) throws Exception {
        log.info("setMaxDt, cube: " + cubeInfo.getName() + ", dt_max: " + dt);
        //
        StoreRecord rec = getRec(cubeInfo);
        mdb.updateRec("Molap_status", UtCnv.toMap("id", rec.getLong("id"), "dt_max", dt));
    }


    // ---
    // Внутренние методы
    // ---

    StoreRecord getRec(CubeInfo cubeInfo) throws Exception {
        StoreRecord rec = mdb.loadQueryRecord("select * from Molap_status where cube_name = :cube_name", UtCnv.toMap("cube_name", cubeInfo.getName()), false);
        if (rec == null || rec.getLong("id") == 0) {
            log.warn("Status record not found for cube: " + cubeInfo.getName());
            //
            mdb.insertRec("Molap_status", UtCnv.toMap("cube_name", cubeInfo.getName()));
            //
            rec = getRec(cubeInfo);
        }
        return rec;
    }


}
