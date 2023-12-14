package kis.molap.model.cli;

import jandcode.commons.*;
import jandcode.commons.cli.*;
import jandcode.core.cli.*;
import jandcode.core.dbm.*;
import jandcode.core.store.*;
import kis.molap.model.worker.*;

public class CubeStatusCmd extends BaseAppCliCmd {


    @Override
    public void exec() throws Exception {
        ModelService modelService = getApp().bean(ModelService.class);
        WorkerService workerService = modelService.getModel().bean(WorkerService.class);
        Worker worker = workerService.createWorker();

        if (audit) {
            Store st = worker.loadInfoAudit();
            UtOutTable.outTable(st);
        } else {
            Store st = worker.loadInfo(cubeName);
            UtOutTable.outTable(st);
        }
    }

    @Override
    public void cliConfigure(CliDef b) {
        b.desc("Информация по состоянию кубов");
        b.opt("cubeName")
                .names("-cube")
                .arg("имя куба")
                .desc("Показать только по указанному кубу");
        b.opt("audit")
                .names("-audit")
                .desc("Показать состояние аудита");
    }

    public void setAudit(boolean audit) {
        this.audit = audit;
    }

    public void setCubeName(String cubeName) {
        this.cubeName = cubeName;
    }

    private boolean audit;

    String cubeName;

}
