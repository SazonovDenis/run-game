package kis.molap.ntbd.model

import jandcode.commons.datetime.*
import jandcode.commons.variant.*
import jandcode.core.apx.test.*
import kis.molap.model.conf.*
import kis.molap.model.service.*
import kis.molap.model.util.*
import kis.molap.model.worker.*
import org.junit.jupiter.api.*

class AuditManagerImpl_Test extends Apx_Test {

    @Test
    void auditManager_loadAudit() throws Exception {
        CubeService cubeService = getModel().bean(CubeService)
        WorkerService workerService = getModel().bean(WorkerService.class);
        AgeManager ageManager = workerService.createAgeManager(mdb);
        AuditManager auditManager = workerService.createAuditManager(mdb);

        // Создаем куб
        String cubeName = "Cube_WorkedTime"
        CubeInfo cubeInfo = cubeService.getCubeInfo(cubeName)

        // Узнаем возраст
        long auditAgeFrom = ageManager.getCubeAgeDone(cubeInfo)
        long auditAgeTo = ageManager.getActualAge()
        println("auditAgeFrom: " + auditAgeFrom + ", auditAgeTo: " + auditAgeTo)

        // Внесем изменения
        println("Внесем изменения, cube: " + cubeName)
        mdb.execQuery("update WellStatus set id = id where well = :well and dbeg <= :dt and dend > :dt", [
                well: 1001,
                dt  : XDate.create("2022-01-01")
        ])

        //
        auditAgeFrom = ageManager.getCubeAgeDone(cubeInfo)
        auditAgeTo = ageManager.getActualAge()
        println("auditAgeFrom: " + auditAgeFrom + ", auditAgeTo: " + auditAgeTo)

        //
        List<IVariantMap> audit = auditManager.loadAudit("WellStatus", 0, auditAgeTo)
        UtMolapPrint.printMapList(audit)
    }

}
