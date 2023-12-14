package kis.molap.model.worker;

import jandcode.core.dbm.mdb.*;

/**
 * Отвечает за особенности хранения аудита и записи результата на конкретной базе.
 * Фабрика менеджеров: аудита, возрастов и записи.
 */
public interface WorkerService {

    Worker createWorker();

    AgeManager createAgeManager(Mdb mdb);

    AuditManager createAuditManager(Mdb mdb);

}
