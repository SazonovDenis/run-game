package kis.molap.ntbd.model.base

import jandcode.commons.datetime.*
import jandcode.core.apx.test.*
import jandcode.core.dbm.mdb.*
import kis.molap.model.conf.*
import kis.molap.model.cube.*
import kis.molap.model.service.*
import kis.molap.model.util.*
import kis.molap.model.worker.*
import kis.molap.model.worker.impl.*
import org.junit.jupiter.api.*

/**
 * Предок для тестов кубов и пространств
 */
public class MolapBase_Test extends Apx_Test {


    Mdb mdbRead
    Mdb mdbWrite

    XDate intervalDbeg = XDate.create("2022-01-01")
    XDate intervalDend = XDate.create("2023-12-31")

    XDate ageMinDt = XDate.create("2022-01-01")
    XDate ageMaxDt = XDate.create("2023-10-03")

    MolapStopWatch sw = new MolapStopWatch()

    protected CubeService cubeService
    protected Worker worker
    protected AgeManager ageManager
    protected AuditManager auditManager


    void setUp() throws Exception {
        super.setUp()

        //
        this.cubeService = getModel().bean(CubeService)

        //
        WorkerService workerService = getModel().bean(WorkerService)
        this.ageManager = workerService.createAgeManager(mdb)
        this.auditManager = workerService.createAuditManager(mdb)

        //
        this.worker = workerService.createWorker()
        mdbRead = ((WorkerImpl) worker).mdbRead
        mdbWrite = ((WorkerImpl) worker).mdbWrite

        //
        if (new File("../_logback.xml").exists()) {
            utils.logOn("../_logback.xml")
        }

        //
        mdbRead.connect()
        mdbWrite.connect()
    }

    Cube createCube(String cubeName, Mdb mdb) {
        Cube cube = cubeService.createCube(cubeName, mdb)
        return cube
    }

    CubeInfo createCubeInfo(String cubeName) {
        CubeInfo cubeInfo = cubeService.getCubeInfo(cubeName)
        return cubeInfo
    }

    Space createSpace(String spaceName, Mdb mdb) {
        Space space = cubeService.createSpace(spaceName, mdb)
        return space
    }

    SpaceInfo createSpaceInfo(String spaceName) {
        SpaceInfo spaceInfo = cubeService.getSpaceInfo(spaceName)
        return spaceInfo
    }

    @Test
    @Disabled
    void printTestSelect() {
        println()
        utils.outTable(mdbWrite.loadQuery("select count(*), sum(workedTime), avg(workedTime) from Cube_WellDt"))
    }

}

