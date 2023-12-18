package kis.molap.ntbd.model;

import jandcode.core.dbm.test.*;
import kis.molap.model.conf.*;
import kis.molap.model.service.*;
import kis.molap.model.worker.*;
import kis.molap.model.worker.impl.*;
import org.junit.jupiter.api.*;

public class CubeService_Test extends Dbm_Test {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.out.println();
        System.out.println("kis-molap-ntbd");
    }

    @Test
    public void printMoService() {
        CubeService cubeService = getModel().bean(CubeService.class);

        System.out.println();
        System.out.println("cube");
        for (CubeInfo cubeInfo : cubeService.getCubes()) {
            System.out.println(cubeInfo.getName() + ", space: " + cubeInfo.getSpace() + ", fields: " + cubeInfo.getFields());
        }

        System.out.println();
        System.out.println("space");
        for (SpaceInfo spaceInfo : cubeService.getSpaces()) {
            System.out.println(spaceInfo.getName() + ", table: " + spaceInfo.getTable() + ", coords: " + spaceInfo.getCoords());
        }
    }

    @Test
    public void connectMoWorker() throws Exception {
        WorkerService workerService = getModel().bean(WorkerService.class);
        WorkerImpl worker = (WorkerImpl) workerService.createWorker();
        worker.connect();
    }

    @Test
    public void calcAuditAll() throws Exception {
        WorkerService workerService = getModel().bean(WorkerService.class);
        WorkerImpl worker = (WorkerImpl) workerService.createWorker();
        worker.calcAuditAll(0, 0);
    }

}
