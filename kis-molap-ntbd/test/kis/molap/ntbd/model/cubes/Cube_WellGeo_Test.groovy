package kis.molap.ntbd.model.cubes

import kis.molap.model.coord.Coord
import kis.molap.model.cube.ICalcData
import kis.molap.model.value.ICalcResultStream
import kis.molap.model.value.impl.CalcResultStreamArray;
import kis.molap.ntbd.model.base.*
import org.junit.jupiter.api.Test;

class Cube_WellGeo_Test extends CubeBase_Test {

    void setUp() throws Exception {
        cubeName = "Cube_WellGeo"
        //
        super.setUp()
        //
        //utils.logOn()
    }

    @Test
    void calcTest() throws Exception {
        cubeName = "Cube_WellGeo"
        ICalcResultStream iCalcResultStream = new CalcResultStreamArray()

        ICalcData cube = createCube()

        Coord coord = Coord.create()
        coord.put("wellGeo", 4367)

        //cube.calc(Arrays.asList(coord), intervalDbeg, intervalDend, iCalcResultStream)
        cube.calc(null, intervalDbeg, intervalDend, iCalcResultStream)

        println iCalcResultStream

    }
}