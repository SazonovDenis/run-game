package kis.molap.model.worker.impl;

import jandcode.commons.*;
import jandcode.commons.variant.*;
import jandcode.core.dbm.mdb.*;
import jandcode.core.store.*;
import kis.molap.model.util.*;
import kis.molap.model.worker.*;

import java.util.*;

public class AuditManagerImpl implements IMdbLinkSet, AuditManager {


    protected Mdb mdb;

    // ---
    // IMdbLinkSet
    // ---

    @Override
    public void setMdb(Mdb mdb) {
        this.mdb = mdb;
    }


    // ---
    // IAuditManager
    // ---

    @Override
    public List<IVariantMap> loadAudit(String tableName, long auditAgeFrom, long auditAgeTo) throws Exception {
        List<IVariantMap> res = new ArrayList<>();

        // Старые варианты записей
        Store stLog = loadAuditLog(tableName, auditAgeFrom, auditAgeTo);
        for (StoreRecord recLog : stLog) {
            IVariantMap rec = fromJson(recLog.getString("dat"));
            rec.put("molap_audit_id", recLog.getValue("molap_audit_id"));
            rec.put("molap_audit_opr", recLog.getValue("molap_audit_opr"));
            res.add(rec);
        }

        // Новые варианты записей
        Store stTable = loadAuditTable(tableName, auditAgeFrom, auditAgeTo);
        UtMolapCube.storeValuesToList(stTable, res);

        //
        return res;
    }


    // ---
    // Внутренние методы
    // ---

    public IVariantMap fromJson(String dat) {
        Map values = (Map) UtJson.fromJson(dat);
        IVariantMap rec = new VariantMapWrap(values);
        return rec;
    }

    /**
     * Старые варианты записей
     */
    public Store loadAuditLog(String tableName, long auditAgeFrom, long auditAgeTo) throws Exception {
        Map params = UtCnv.toMap("ageFrom", auditAgeFrom, "ageTo", auditAgeTo);

        //
        String sqlAudit = """
                select
                    Molap_audit.tableName,
                    Molap_audit.id molap_audit_id,
                    Molap_audit.opr molap_audit_opr,
                    Molap_audit.dat
                from
                    Molap_audit
                where
                    Molap_audit.id >= :ageFrom and
                    Molap_audit.id <= :ageTo and
                    Molap_audit.tableName = '$tableName$'
                order by
                    id""";
        String sql = sqlAudit.replace("$tableName$", tableName);

        //
        Store st = mdb.loadQuery(sql, params);

        //
        return st;
    }

    /**
     * Новые варианты записей
     */
    public Store loadAuditTable(String tableName, long auditAgeFrom, long auditAgeTo) throws Exception {
        Map params = UtCnv.toMap("ageFrom", auditAgeFrom, "ageTo", auditAgeTo);

        //
        String sqlTable = """
                select
                    DataTable.*,
                    Molap_audit.opr molap_audit_opr,
                    Molap_audit.id molap_audit_id
                from
                    $tableName$ DataTable
                    join Molap_audit on (DataTable.id = Molap_audit.tableId and Molap_audit.opr = 2)
                where
                    Molap_audit.id >= :ageFrom and
                    Molap_audit.id <= :ageTo and
                    Molap_audit.tableName = '$tableName$'
                order by
                    Molap_audit.id""";
        String sql = sqlTable.replace("$tableName$", tableName);

        //
        Store st = mdb.loadQuery(sql, params);

        //
        return st;
    }


}
