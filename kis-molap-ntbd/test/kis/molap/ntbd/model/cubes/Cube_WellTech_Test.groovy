package kis.molap.ntbd.model.cubes

import jandcode.core.store.Store
import kis.molap.model.coord.Coord
import kis.molap.model.cube.ICalcData
import kis.molap.model.value.ICalcResultStream
import kis.molap.model.value.impl.CalcResultStreamArray;
import kis.molap.ntbd.model.base.*
import org.junit.jupiter.api.Test;

class Cube_WellTech_Test extends CubeBase_Test {

    void setUp() throws Exception {
        cubeName = "Cube_WellTech"
        //
        super.setUp()
        //
        //utils.logOn()
    }

    @Test
    void calcTest() throws Exception {
        ICalcResultStream iCalcResultStream = new CalcResultStreamArray()

        ICalcData cube = createCube()

        Coord coord = Coord.create()
        coord.put("wellTech", 4394)

        //cube.calc(Arrays.asList(coord), intervalDbeg, intervalDend, iCalcResultStream)
        cube.calc(null, intervalDbeg, intervalDend, iCalcResultStream)

        println iCalcResultStream
    }

    @Test
    void storeTest() throws Exception {
        
        Store stRating = mdb.createStore("Well.rating.load")

    }
}