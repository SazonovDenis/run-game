package kis.molap.model.cube.impl;

import jandcode.commons.*;
import jandcode.commons.error.*;
import jandcode.core.dbm.mdb.*;
import kis.molap.model.conf.*;
import kis.molap.model.coord.*;
import kis.molap.model.cube.*;
import kis.molap.model.util.*;
import kis.molap.model.worker.*;
import kis.molap.model.worker.impl.*;
import org.slf4j.*;

public abstract class CubeCustom implements Cube {


    protected Mdb mdb;

    protected AgeManager ageManager;
    protected AuditManager auditManager;

    protected CubeInfo cubeInfo;

    protected MolapStopWatch sw = new MolapStopWatch();

    protected static Logger log = UtLog.getLogConsole();

    protected LogerFiltered logCube = new LogerFiltered(log);


    // ---
    // ICubeInfo
    // ---

    @Override
    public CubeInfo getInfo() {
        return cubeInfo;
    }


    public void setInfo(CubeInfo cubeInfo) {
        this.cubeInfo = cubeInfo;

        //
        this.logCube.info = "cube: " + cubeInfo.getName();
    }


    // ---
    // ICalcCoords
    // ---

    @Override
    public void convertCoords(String sourceCubeName, CoordList coords, CoordList coordsRes) throws Exception {
        throw new XError("Not possible to convert coords, sourceCube: " + sourceCubeName + ", destinationCube: " + getInfo().getName());
    }


    // ---
    // Внутренние методы
    // ---

    public void setMdb(Mdb mdb) {
        this.mdb = mdb;

        //
        WorkerService workerService = mdb.getModel().bean(WorkerService.class);

        //
        this.ageManager = workerService.createAgeManager(mdb);
        this.auditManager = workerService.createAuditManager(mdb);

        //
        logCube.LOG_STEP_SIZE = 1000;
    }


}
