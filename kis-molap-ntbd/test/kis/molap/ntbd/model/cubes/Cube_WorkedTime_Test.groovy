package kis.molap.ntbd.model.cubes

import jandcode.commons.datetime.*
import jandcode.core.store.*
import kis.molap.model.coord.*
import kis.molap.ntbd.model.*
import kis.molap.ntbd.model.base.*

public class Cube_WorkedTime_Test extends CubeBase_Test {


    void setUp() throws Exception {
        cubeName = "Cube_WorkedTime"
        //
        super.setUp()
    }

    @Override
    void makeChanges_for_Audit() {
        println("Внесем изменения, cube: " + cubeName)

        StoreRecord rec_WellStatus_1 = mdb.loadQuery("select * from WellStatus where well = :well and dbeg <= :dt and dend > :dt", [
                well: 1001,
                dt  : XDate.create("2022-01-22")
        ]).get(0)
        mdb.outTable(rec_WellStatus_1)

        mdb.execQuery("update WellStatus set id = id where id = :id", [
                id: rec_WellStatus_1.getLong("id"),
        ])
    }

    @Override
    CoordList getCoords_for_byOneCoord() {
        Store st = mdb.loadQuery("select * from well where id > 1000 limit 2")
        List<Long> ids = CubeDataUtils.toListLong(st.getUniqueValues("id"))

        //
        CoordList coords = CoordList.create()
        for (Long id : ids) {
            Coord coord = Coord.create()
            coord.put("well", id)
            coords.add(coord)
        }

        //
        return coords
    }


}

