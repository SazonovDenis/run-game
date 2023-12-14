package kis.molap.ntbd.model;

import jandcode.commons.*;
import jandcode.core.dbm.*;
import jandcode.core.dbm.test.*;
import jandcode.core.store.*;
import kis.molap.model.worker.*;
import org.junit.jupiter.api.*;

public class Cube_Test extends Dbm_Test {


    @Test
    public void info() throws Exception {
        ModelService modelService = app.bean(ModelService.class);
        WorkerService workerService = modelService.getModel().bean(WorkerService.class);
        Worker worker = workerService.createWorker();

        Store stInfo = worker.loadInfo(null);
        UtOutTable.outTable(stInfo);

        Store stInfoAudit = worker.loadInfoAudit();
        UtOutTable.outTable(stInfoAudit);
    }

}
