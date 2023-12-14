package kis.molap.ntbd.model.cubes


import kis.molap.model.conf.*
import kis.molap.model.coord.*
import kis.molap.model.cube.*
import kis.molap.model.util.*
import kis.molap.model.value.*
import kis.molap.model.value.impl.*
import kis.molap.ntbd.model.base.*
import org.junit.jupiter.api.*

import static org.junit.jupiter.api.Assertions.*

class Cube_OrgParkProdTest extends CubeBase_Test {

    void setUp() throws Exception {
        cubeName = "Cube_OrgParkProd"
        //
        super.setUp()
    }

    @Test
    void calcTest() throws Exception {
        cubeName = "Cube_OrgParkProd"

        ICalcResultStream iCalcResultStream = new CalcResultStreamArray()

        ICalcData cube = createCube()

        cube.calc(null, intervalDbeg, intervalDend, iCalcResultStream)

    }
}
