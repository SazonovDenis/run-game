package kis.molap.ntbd.model.cubes

import jandcode.commons.datetime.*
import jandcode.core.store.*
import kis.molap.model.coord.*
import kis.molap.ntbd.model.base.*

class Cube_OrgProd_WaterInj_Direct_Test extends CubeBase_Test {


    void setUp() throws Exception {
        cubeName = "Cube_OrgProd_WaterInj_Direct"

        intervalDbeg = XDate.create("2023-01-01")
        intervalDend = XDate.create("2023-01-01")

        super.setUp()
    }


    @Override
    CoordList getCoords_for_byOneCoord() {
        CoordList coords = CoordList.create()

        Coord coord = Coord.create()
        coord.put("org", 1010)
        coord.put("dt", XDate.create("2023-03-01"))
        coords.add(coord)

        return coords
    }


    @Override
    void makeChanges_for_Audit() {
        println("Внесем изменения, cube: " + cubeName)

        Store sr_Wells = mdb.loadQuery("select * from Well where id > 1000 limit 2")

        StoreRecord rec_LiquidProd_1 = mdb.loadQuery("select * from LiquidProd where well = :well and dbeg <= :dt and dend > :dt", [
                well: sr_Wells.get(0).getLong("id"),
                dt  : XDate.create("2022-01-22")
        ]).get(0)
        mdb.outTable(rec_LiquidProd_1)

        StoreRecord rec_WellGeo_1 = mdb.loadQuery("select * from WellGeo where well = :well and dbeg <= :dt and dend > :dt", [
                well: sr_Wells.get(1).getLong("id"),
                dt  : XDate.create("2022-01-22")
        ]).get(0)
        mdb.outTable(rec_WellGeo_1)

        mdb.execQuery("update LiquidProd set id = id where id = :id", [
                id: rec_LiquidProd_1.getLong("id"),
        ])
        mdb.execQuery("update WellGeo set id = id where id = :id", [
                id: rec_WellGeo_1.getLong("id"),
        ])
    }


}