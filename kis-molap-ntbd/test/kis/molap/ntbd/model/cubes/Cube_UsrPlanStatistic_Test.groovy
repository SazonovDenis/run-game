package kis.molap.ntbd.model.cubes

import kis.molap.model.coord.Coord
import kis.molap.model.coord.CoordList
import kis.molap.model.coord.impl.CoordImpl
import kis.molap.model.coord.impl.CoordListImpl
import kis.molap.ntbd.model.base.*

public class Cube_UsrPlanStatistic_Test extends CubeBase_Test {

    void setUp() throws Exception {
        cubeName = "Cube_UsrPlanStatistic"
        //
        super.setUp()
    }

    CoordList getCoords_for_byOneCoord() {
        CoordList res = new CoordListImpl()
        Coord coord = new CoordImpl()
        coord.put("usr", 1000)
        coord.put("plan", 1002)
        res.add(coord)
        return res
    }

}

