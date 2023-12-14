package kis.molap.ntbd.model;

import jandcode.commons.*;
import jandcode.commons.datetime.*;
import jandcode.core.dbm.*;
import jandcode.core.dbm.test.*;
import jandcode.core.store.*;
import kis.molap.model.conf.*;
import kis.molap.model.worker.*;
import kis.molap.model.worker.impl.*;
import org.junit.jupiter.api.*;

import java.util.*;

public class Worker_Test extends Dbm_Test {


    XDate intervalDbeg = XDate.create("2022-01-01");
    XDate intervalDend = XDate.create("2023-03-01");
    //String cubeName = "Cube_WorkedTime";
    String cubeName = "Cube_WellProd";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        utils.logOn("../_logback.xml");
    }

    @Test
    public void testOne() throws Exception {
        ModelService modelService = app.bean(ModelService.class);
        WorkerService workerService = modelService.getModel().bean(WorkerService.class);
        Worker worker = workerService.createWorker();

        //
        Store st0 = worker.loadInfo(cubeName);
        UtOutTable.outTable(st0);

        //
        System.out.println();
        System.out.println("calcIntervalCube");
        worker.calcIntervalCube(cubeName, intervalDbeg, intervalDend, true);

        //
        System.out.println();
        System.out.println("calcAuditCube");
        worker.calcAuditCube(cubeName, 0, 0, true);

        //
        Store st3 = worker.loadInfo(cubeName);
        UtOutTable.outTable(st3);
    }

    @Test
    public void testOne_NoCascade() throws Exception {
        ModelService modelService = app.bean(ModelService.class);
        WorkerService workerService = modelService.getModel().bean(WorkerService.class);
        Worker worker = workerService.createWorker();

        //
        Store st0 = worker.loadInfo(cubeName);
        UtOutTable.outTable(st0);

        //
        System.out.println();
        System.out.println("calcIntervalCube");
        worker.calcIntervalCube(cubeName, intervalDbeg, intervalDend, false);

        //
        System.out.println();
        System.out.println("calcAuditCube");
        worker.calcAuditCube(cubeName, 0, 0, false);

        //
        Store st3 = worker.loadInfo(cubeName);
        UtOutTable.outTable(st3);
    }

    @Test
    public void testAll() throws Exception {
        ModelService modelService = app.bean(ModelService.class);
        WorkerService workerService = modelService.getModel().bean(WorkerService.class);
        Worker worker = workerService.createWorker();

        //
        Store st0 = worker.loadInfo(null);
        UtOutTable.outTable(st0);

        //
        System.out.println();
        System.out.println("calcIntervalAll");
        worker.calcIntervalAll(intervalDbeg, intervalDend);

        //
        System.out.println();
        System.out.println("calcAuditAll");
        worker.calcAuditAll(0, 0);

        //
        Store st3 = worker.loadInfo(null);
        UtOutTable.outTable(st3);

        //
        printTestSelect();
    }

    @Test
    public void auditAll() throws Exception {
        ModelService modelService = app.bean(ModelService.class);
        WorkerService workerService = modelService.getModel().bean(WorkerService.class);
        Worker worker = workerService.createWorker();

        //
        Store st0 = worker.loadInfo(null);
        UtOutTable.outTable(st0);

        //
        System.out.println();
        System.out.println("calcAuditAll");
        worker.calcAuditAll(0, 0);

        //
        Store st3 = worker.loadInfo(null);
        UtOutTable.outTable(st3);

        //
        printTestSelect();
    }

    @Test
    void printTestSelect() throws Exception {
        utils.outTable(getMdb().loadQuery("select count(*), count(distinct well) countWell, count(distinct dt) countDt, sum(workedTime) workedTimeSum, avg(workedTime) workedTimeAvg, sum(oil) oilSum, avg(oil) oilAvg from Cube_WellDt"));
    }

    @Test
    public void markAuditCube() throws Exception {
        ModelService modelService = app.bean(ModelService.class);
        WorkerService workerService = modelService.getModel().bean(WorkerService.class);
        Worker worker = workerService.createWorker();

        Store st = worker.loadInfo(cubeName);
        UtOutTable.outTable(st);

        worker.markAuditCube(cubeName, 1);

        st = worker.loadInfo(cubeName);
        UtOutTable.outTable(st);
    }

    @Test
    public void parseSortCubeNames() throws Exception {
        ModelService modelService = app.bean(ModelService.class);
        WorkerService workerService = modelService.getModel().bean(WorkerService.class);
        WorkerImpl worker = (WorkerImpl) workerService.createWorker();

        String cubeNames = "Cube_OrgProd,Cube_OrgProd,Cube_OrgProd_WaterProd,Cube_OrgProd_WaterInj,Cube_OrgProd_Direct,Cube_OrgProd_WaterInj_Direct,Cube_OrgProd_WaterProd_Direct";
        List<CubeInfo> lst = worker.parseSortCubeNames(cubeNames, false);
        for (CubeInfo cube : lst) {
            System.out.println(cube.getName());
        }
        System.out.println();

        cubeNames = "Cube_OrgProd,Cube_OrgProd_WaterProd,Cube_OrgProd_WaterInj,Cube_OrgProd_Direct,Cube_OrgProd_WaterInj_Direct,Cube_OrgProd_WaterProd_Direct";
        lst = worker.parseSortCubeNames(cubeNames, false);
        for (CubeInfo cube : lst) {
            System.out.println(cube.getName());
        }
    }

}
