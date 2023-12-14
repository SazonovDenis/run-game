package kis.molap.model.worker.impl;

import jandcode.commons.conf.*;
import jandcode.core.*;
import jandcode.core.dbm.*;
import jandcode.core.dbm.mdb.*;
import kis.molap.model.service.*;
import kis.molap.model.worker.*;


public class WorkerServiceImpl extends BaseModelMember implements WorkerService {


    CubeService cubeService;

    private Conf workerConf;
    private Conf ageManagerConf;
    private Conf auditManageConf;


    @Override
    protected void onConfigure(BeanConfig cfg) throws Exception {
        super.onConfigure(cfg);

        //
        cubeService = getModel().bean(CubeService.class);

        //
        this.workerConf = cfg.getConf().getConf("worker/default");
        this.ageManagerConf = cfg.getConf().getConf("ageManager/default");
        this.auditManageConf = cfg.getConf().getConf("auditManager/default");
    }


    @Override
    public Worker createWorker() {
        Worker worker = (Worker) getModel().create(workerConf);
        return worker;
    }


    @Override
    public AgeManager createAgeManager(Mdb mdb) {
        AgeManager ageManager = (AgeManager) getModel().create(ageManagerConf);
        ageManager.setMdb(mdb);
        return ageManager;
    }

    @Override
    public AuditManager createAuditManager(Mdb mdb) {
        AuditManager auditManager = (AuditManager) getModel().create(auditManageConf);
        auditManager.setMdb(mdb);
        return auditManager;
    }


}
