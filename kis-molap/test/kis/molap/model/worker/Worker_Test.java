package kis.molap.model.worker;

import jandcode.commons.*;
import jandcode.core.dbm.*;
import jandcode.core.dbm.test.*;
import jandcode.core.store.*;
import org.junit.jupiter.api.*;

public class Worker_Test extends Dbm_Test {

    @Test
    public void loadInfo() throws Exception {
        ModelService modelService = app.bean(ModelService.class);
        WorkerService workerService = modelService.getModel().bean(WorkerService.class);
        Worker worker = workerService.createWorker();

        Store st = worker.loadInfo(null);
        UtOutTable.outTable(st);
    }

    @Test
    public void loadInfoAudit() throws Exception {
        ModelService modelService = app.bean(ModelService.class);
        WorkerService workerService = modelService.getModel().bean(WorkerService.class);
        Worker worker = workerService.createWorker();

        Store st = worker.loadInfoAudit();
        UtOutTable.outTable(st);
    }

}
