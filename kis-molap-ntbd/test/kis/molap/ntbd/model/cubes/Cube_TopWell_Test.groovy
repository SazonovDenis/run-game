package kis.molap.ntbd.model.cubes

import jandcode.commons.datetime.*
import kis.molap.model.cube.*
import kis.molap.model.value.*
import kis.molap.model.value.impl.*
import kis.molap.ntbd.model.base.*
import org.junit.jupiter.api.*

class Cube_TopWell_Test extends CubeBase_Test {

    void setUp() throws Exception {
        cubeName = "Cube_TopWell"
        super.setUp();

        intervalDbeg = XDate.create("2023-01-01")
        intervalDend = XDate.create("2023-11-29")
        utils.logOff()
    }

    @Test
    void calcTest() {
        cubeName = "Cube_TopWell"
        println("cube: " + cubeName)

        ICalcResultStream iCalcResultStream = new CalcResultStreamArray()
        ICalcData cube = createCube()
        cube.calc(null, intervalDbeg, intervalDend, iCalcResultStream)
    }

}