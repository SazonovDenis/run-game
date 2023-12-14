package kis.molap.model.worker.impl;

import jandcode.commons.*;
import jandcode.commons.datetime.*;
import jandcode.commons.variant.*;
import jandcode.core.dbm.mdb.*;
import jandcode.core.store.*;
import kis.molap.model.conf.*;
import kis.molap.model.util.*;

import java.util.*;

public class UtWorkerLogDb implements IMdbLinkSet {

    private Mdb mdb;

    @Override
    public void setMdb(Mdb mdb) {
        this.mdb = mdb;
    }

    public void writeLog_interval(MolapStopWatch sw, CubeInfo cubeInfo, XDate intervalDbeg, XDate intervalDend) throws Exception {
        StoreRecord rec = mdb.createStoreRecord("Molap_log");
        //
        rec.setValue("cube_name", cubeInfo.getName());
        rec.setValue("calc_type", CalcType.CALC_INTERVAL);
        rec.setValue("interval_dbeg", intervalDbeg);
        rec.setValue("interval_dend", intervalDend);
        rec.setValue("calc_start", sw.getStart("total"));
        rec.setValue("calc_stop", sw.getStop("total"));
        rec.setValue("interval_count", sw.getLong("dirty", "count"));
        rec.setValue("interval_count_write", sw.getLong("write", "count"));
        rec.setValue("interval_duration_dirty", sw.getDuration("dirty"));
        rec.setValue("interval_duration_calc", sw.getDuration("calc"));
        rec.setValue("interval_duration_write", sw.getDuration("write"));
        //
        mdb.insertRec("Molap_log", rec);
    }

    public void writeLog_audit(MolapStopWatch sw, CubeInfo cubeInfo, long auditAgeFrom, long auditAgeTo) throws Exception {
        StoreRecord rec = mdb.createStoreRecord("Molap_log");
        //
        rec.setValue("cube_name", cubeInfo.getName());
        rec.setValue("calc_type", CalcType.CALC_AUDIT);
        rec.setValue("audit_age_from", auditAgeFrom);
        rec.setValue("audit_age_to", auditAgeTo);
        rec.setValue("calc_start", sw.getStart("total"));
        rec.setValue("calc_stop", sw.getStop("total"));
        rec.setValue("audit_count_own", sw.getLong("dirty", "count"));
        rec.setValue("audit_count_depends", sw.getLong("dirty_depends", "count"));
        rec.setValue("audit_count_write", sw.getLong("write", "count"));
        rec.setValue("audit_duration_dirty", sw.getDuration("dirty"));
        rec.setValue("audit_duration_dirty_depends", sw.getDuration("dirty_depends"));
        rec.setValue("audit_duration_convert", sw.getDuration("convert"));
        rec.setValue("audit_duration_calc", sw.getDuration("calc"));
        rec.setValue("audit_duration_write", sw.getDuration("write"));
        //
        mdb.insertRec("Molap_log", rec);
    }

    public void writeLog_interval_error(String cubeName, XDate intervalDbeg, XDate intervalDend, Exception e) throws Exception {
        StoreRecord rec = mdb.createStoreRecord("Molap_log");
        //
        rec.setValue("cube_name", cubeName);
        rec.setValue("calc_type", CalcType.CALC_INTERVAL);
        rec.setValue("interval_dbeg", intervalDbeg);
        rec.setValue("interval_dend", intervalDend);
        XDateTime dtNow = XDateTime.now();
        rec.setValue("calc_start", dtNow);
        rec.setValue("calc_stop", dtNow);
        rec.setValue("calc_error", e.getMessage());
        //
        mdb.insertRec("Molap_log", rec);
    }

    public void writeLog_audit_error(String cubeName, long auditAgeFrom, long auditAgeTo, Exception e) throws Exception {
        StoreRecord rec = mdb.createStoreRecord("Molap_log");
        //
        rec.setValue("cube_name", cubeName);
        rec.setValue("calc_type", CalcType.CALC_AUDIT);
        rec.setValue("audit_age_from", auditAgeFrom);
        rec.setValue("audit_age_to", auditAgeTo);
        XDateTime dtNow = XDateTime.now();
        rec.setValue("calc_start", dtNow);
        rec.setValue("calc_stop", dtNow);
        rec.setValue("calc_error", e.getMessage());
        //
        mdb.insertRec("Molap_log", rec);
    }

    public void writeLog_setDtExpand(String cubeName, XDateTime dtExpand) throws Exception {
        StoreRecord rec = mdb.createStoreRecord("Molap_log");
        //
        XDateTime dtNow = XDateTime.now();
        rec.setValue("cube_name", cubeName);
        rec.setValue("calc_type", CalcType.SET_DT_EXPAND);
        rec.setValue("calc_start", dtNow);
        rec.setValue("calc_stop", dtNow);
        rec.setValue("interval_dend", dtExpand);
        //
        mdb.insertRec("Molap_log", rec);
    }

    public void writeLog_setDtActual(String cubeName, XDateTime dtActual) throws Exception {
        StoreRecord rec = mdb.createStoreRecord("Molap_log");
        //
        XDateTime dtNow = XDateTime.now();
        rec.setValue("cube_name", cubeName);
        rec.setValue("calc_type", CalcType.SET_DT_ACTUAL);
        rec.setValue("calc_start", dtNow);
        rec.setValue("calc_stop", dtNow);
        rec.setValue("audit_dend", dtActual);
        //
        mdb.insertRec("Molap_log", rec);
    }

    public void updateStatistic(long recordCount, double durationTotal, CubeInfo cubeInfo, IVariantMap statistic) throws Exception {
        if (recordCount > 0) {
            Map params = UtCnv.toMap("cube_name", cubeInfo.getName());

            //
            String sqlSet = "one_rec_cost = :one_rec_cost";
            params.put("one_rec_cost", durationTotal / recordCount);

            //
            if (statistic.getLong("recordPeriod") > 0) {
                sqlSet = sqlSet + ", rec_rate = :rec_rate";
                params.put("rec_rate", 1000 * 60 * 60 * 24 * recordCount / statistic.getLong("recordPeriod"));
            }
            if (statistic.containsKey("durationDirty")) {
                sqlSet = sqlSet + ", one_rec_durationDirty = :one_rec_durationDirty";
                params.put("one_rec_durationDirty", statistic.getDouble("durationDirty") / recordCount);
            }
            if (statistic.containsKey("durationCalc")) {
                sqlSet = sqlSet + ", one_rec_durationCalc = :one_rec_durationCalc";
                params.put("one_rec_durationCalc", statistic.getDouble("durationCalc") / recordCount);
            }
            if (statistic.containsKey("durationWrite")) {
                sqlSet = sqlSet + ", one_rec_durationWrite = :one_rec_durationWrite";
                params.put("one_rec_durationWrite", statistic.getDouble("durationWrite") / recordCount);
            }

            //
            String sql = "update Molap_statistic set " + sqlSet + " where cube_name = :cube_name";

            //
            mdb.execQuery(sql, params);
        }
    }


}
