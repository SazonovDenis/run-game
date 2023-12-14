package kis.molap.ntbd.model.cubes

import jandcode.commons.datetime.*
import jandcode.core.store.*
import kis.molap.model.coord.*
import kis.molap.ntbd.model.*
import kis.molap.ntbd.model.base.*
import org.junit.jupiter.api.*

import static org.junit.jupiter.api.Assertions.*

public class Cube_DensityOilWell_Test extends CubeBase_Test {


    XDate dt
    long well
    StoreRecord rec_DensityOil
    StoreRecord rec_WellGeo
    StoreRecord rec_Cube_WellDt

    XDate dt_fromGeo
    long well_fromGeo
    StoreRecord rec_Cube_WellDt_fromGeo


    void setUp() throws Exception {
        cubeName = "Cube_DensityOilWell"
        //
        super.setUp()
        //
        //utils.logOn()
    }


    @Test
    void byAudit_Worker() throws Exception {
        super.byAudit_Worker()
    }


    @Override
    void makeChanges_for_Audit() {
        Cube_DensityOilGeo_Test test_DensityOilGeo = new Cube_DensityOilGeo_Test()
        test_DensityOilGeo.dbm = this.dbm
        test_DensityOilGeo.setUp()
        //
        //test_DensityOilGeo.makeChanges_for_Audit()
        test_DensityOilGeo.byAudit_Worker()
        //
        well_fromGeo = test_DensityOilGeo.well
        dt_fromGeo = test_DensityOilGeo.dt
        //
        println("from Geo well: " + well_fromGeo)
        println("from Geo dt: " + dt_fromGeo)


        // ---
        // Выбираем что изменять

        // Выберем такую скважину, у которой в указанную дату указана плотность нефти
        // и есть привязка к Org (т.е. скважина существует в выбранный период)
        dt = XDate.create("2022-02-01")
        rec_DensityOil = mdb.loadQuery("select DensityOil.* from DensityOil join WellOrg on (DensityOil.well = WellOrg.well and WellOrg.dbeg <= :dt and WellOrg.dend > :dt) where DensityOil.dbeg <= :dt and DensityOil.dend > :dt order by DensityOil.well limit 1", [
                dt: dt
        ]).get(0)
        well = rec_DensityOil.getLong("well")
        //
        rec_WellGeo = mdb.loadQuery("select * from WellGeo where well = :well and dbeg <= :dt and dend > :dt", [
                well: well,
                dt  : XDate.create("2022-01-22")
        ]).get(0)
        // Что сейчас в кубе
        rec_Cube_WellDt = mdb.loadQuery(sql_Cube_WellDt(), [
                well: well,
                dt  : dt,
        ]).get(0)
        //
        rec_Cube_WellDt_fromGeo = mdb.loadQuery(sql_Cube_WellDt(), [
                well: well_fromGeo,
                dt  : dt_fromGeo,
        ]).get(0)

        //
        println()
        println("----------")
        println("До изменений, cube: " + cubeName)
        println()
        println("DensityOil")
        mdb.outTable(rec_DensityOil)
        println("WellGeo")
        mdb.outTable(rec_WellGeo)
        println("Cube_WellDt")
        mdb.outTable(rec_Cube_WellDt)
        println("Cube_WellDt_fromGeo")
        mdb.outTable(rec_Cube_WellDt_fromGeo)

        // ---
        // Внесем изменения
        println()
        println("----------")
        println("Внесем изменения, cube: " + cubeName)

        // DensityOil
        double densityOil = 0.8 + Math.round(Math.random() * 2000) / 10000
        println("DensityOil.val: " + rec_DensityOil.getDouble("val") + " -> " + densityOil)
        mdb.execQuery("update DensityOil set val = :densityOil where id = :id", [
                id        : rec_DensityOil.getLong("id"),
                densityOil: densityOil
        ])

        // WellGeo
/*
        mdb.execQuery("update WellGeo set id = id where id = :id", [
                id: rec_WellGeo.getLong("id"),
        ])
*/

        //
        println()
    }


    @Override
    void checkChanges_after_Audit() {
        //
        StoreRecord rec_DensityOil_1 = mdb.loadQuery("select * from DensityOil where well = :well and DensityOil.dbeg <= :dt and DensityOil.dend > :dt limit 1", [
                well: well,
                dt  : dt,
        ]).get(0)
        //
        StoreRecord rec_Cube_WellDt_1 = mdb.loadQuery(sql_Cube_WellDt(), [
                well: well,
                dt  : dt,
        ]).get(0)
        StoreRecord rec_Cube_WellDt_fromGeo_1 = mdb.loadQuery(sql_Cube_WellDt(), [
                well: well_fromGeo,
                dt  : dt_fromGeo,
        ]).get(0)

        //
        println("----------")
        println("После изменений")
        println()
        println("DensityOil")
        mdb.outTable(rec_DensityOil_1)
        println("Cube_WellDt")
        mdb.outTable(rec_Cube_WellDt_1)
        println("Cube_WellDt_fromGeo")
        mdb.outTable(rec_Cube_WellDt_fromGeo_1)

        //
        assertEquals(Math.round(10000 * rec_DensityOil_1.getDouble("val")), Math.round(10000 * rec_Cube_WellDt_1.getDouble("densityOil")), "Измененная densityOil не попала в Cube_WellDt")
        assertEquals(false, rec_DensityOil.getDouble("val") == rec_DensityOil_1.getDouble("val"), "densityOil не была изменена в DensityOil")
        assertEquals(false, rec_Cube_WellDt.getDouble("densityOil") == rec_Cube_WellDt_1.getDouble("densityOil"), "densityOil не была изменена в Cube_WellDt")
        assertEquals(false, rec_Cube_WellDt_fromGeo.getDouble("densityOil") == rec_Cube_WellDt_fromGeo_1.getDouble("densityOil"), "densityOil (from Geo) не была изменена в Cube_WellDt")
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


    String sql_Cube_WellDt() {
        return """
select
    well, dt, densityOil, liquidProd, liquidProdBsw, workedTime, oil 
from 
    Cube_WellDt
where
    well = :well and dt = :dt
"""
    }


}

