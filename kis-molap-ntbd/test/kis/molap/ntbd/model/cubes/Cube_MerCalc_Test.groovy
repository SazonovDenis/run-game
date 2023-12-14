package kis.molap.ntbd.model.cubes

import jandcode.commons.datetime.*
import kis.molap.model.coord.*
import kis.molap.model.coord.impl.*
import kis.molap.ntbd.model.base.*

class Cube_MerCalc_Test extends CubeBase_Test {

    void setUp() throws Exception {
        cubeName = "Cube_MerCalc"
        //
        super.setUp()
        //
        //utils.logOn()
    }

    CoordList getCoords_for_byOneCoord() {
        CoordList coords = CoordList.create()
        Coord coord

        //
        coord = Coord.create()
        coord.putAll(["well": 1001, "dt": XDate.create("2023-01-01")])
        coords.add(coord)

        //
        coord = Coord.create()
        coord.putAll(["well": 1020, "dt": new Period(XDate.create("2023-01-01"), XDate.create("2023-03-01"))])
        coords.add(coord)

        //
        coord = Coord.create()
        coord.putAll(["well": 1025, "dt": new Period(XDate.create("2023-01-01"), XDate.create("2023-03-01"))])
        coords.add(coord)

        //
        return coords
    }

}
