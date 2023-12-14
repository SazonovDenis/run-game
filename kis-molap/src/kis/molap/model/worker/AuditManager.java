package kis.molap.model.worker;

import jandcode.commons.variant.*;
import jandcode.core.dbm.mdb.*;

import java.util.*;

/**
 * Извлекаем аудит
 */
public interface AuditManager extends IMdbLinkSet {

    /**
     * Возвращает store, в котором содержатся измененные записи.
     * Старый и новый варианты
     */
    List<IVariantMap> loadAudit(String tableName, long auditAgeFrom, long auditAgeTo) throws Exception;

}
