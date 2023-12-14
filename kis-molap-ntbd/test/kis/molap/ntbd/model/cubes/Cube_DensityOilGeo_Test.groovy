package kis.molap.ntbd.model.cubes

import jandcode.commons.datetime.*
import jandcode.core.store.*
import kis.molap.model.coord.*
import kis.molap.ntbd.model.*
import kis.molap.ntbd.model.base.*
import org.junit.jupiter.api.*

import static org.junit.jupiter.api.Assertions.*

public class Cube_DensityOilGeo_Test extends CubeBase_Test {

    XDate dt
    long geo
    long well
    StoreRecord rec_GeoData
    StoreRecord rec_GeoAttr
    StoreRecord rec_Cube_GeoDt


    void setUp() throws Exception {
        cubeName = "Cube_DensityOilGeo"
        //
        super.setUp()
        //
        //utils.logOn()
    }

    ////////////////////
    // ^c откуда берется эти значения по плотности??? все время разные
    ////////////////////

    
    @Test
    void byAudit_Worker() throws Exception {
        super.byAudit_Worker()
    }


    @Override
    void makeChanges_for_Audit() {
        // ---
        // Выбираем что изменять

        // Выберем такой горизонт, у которго в указанную дату есть как скважины, так и GeoData,
        // и есть привязка к Org (т.е. скважины существуют в выбранный период)
        dt = XDate.create("2022-02-01")
        StoreRecord rec_Geo = mdb.loadQuery("select GeoAttr.* from GeoAttr join GeoData on (GeoAttr.geo = GeoData.geo and GeoData.dbeg <= :dt and GeoData.dend > :dt) join WellGeo on (GeoAttr.geo = WellGeo.geo and WellGeo.dbeg <= :dt and WellGeo.dend > :dt) join WellOrg on (WellGeo.well = WellOrg.well and WellOrg.dbeg <= :dt and WellOrg.dend > :dt) left join DensityOil on (DensityOil.well = WellGeo.well and DensityOil.dbeg <= :dt and DensityOil.dend > :dt) where GeoAttr.geoType = :geoType and GeoAttr.dbeg <= :dt and GeoAttr.dend > :dt and DensityOil.id is null order by GeoAttr.geo limit 1", [
                geoType: 4, // GeoType_consts.horizon = 4,
                dt     : dt,
        ]).get(0)
        geo = rec_Geo.getLong("geo")
        // Выберем скважину горизонта, у которой
        // есть привязка к Org (т.е. скважина существует в выбранный период)
        // но отсутствует явное указание плотности через DensityOil
        StoreRecord rec_WellGeo = mdb.loadQuery("select WellGeo.* from WellGeo join WellOrg on (WellGeo.well = WellOrg.well and WellOrg.dbeg <= :dt and WellOrg.dend > :dt) left join DensityOil on (DensityOil.well = WellGeo.well and DensityOil.dbeg <= :dt and DensityOil.dend > :dt) where WellGeo.geo = :geo and WellGeo.dbeg <= :dt and WellGeo.dend > :dt and DensityOil.id is null order by WellGeo.well limit 1", [
                geo: geo,
                dt : dt,
        ]).get(0)
        well = rec_WellGeo.getLong("well")
        //
        rec_GeoData = mdb.loadQuery("select * from GeoData where geo = :geo and GeoData.dbeg <= :dt and GeoData.dend > :dt limit 1", [
                geo: geo,
                dt : dt,
        ]).get(0)
        rec_GeoAttr = mdb.loadQuery("select * from GeoAttr where geo = :geo and GeoAttr.dbeg <= :dt and GeoAttr.dend > :dt limit 1", [
                geo: geo,
                dt : dt,
        ]).get(0)
        // Что сейчас в кубе
        rec_Cube_GeoDt = mdb.loadQuery(sql_Cube_GeoDt(), [
                geo: geo,
                dt : dt,
        ]).get(0)
        //
        println()
        println("----------")
        println("До изменений, cube: " + cubeName)
        println()
        println("WellGeo")
        mdb.outTable(rec_WellGeo)
        println("GeoData")
        mdb.outTable(rec_GeoData)
        println("Cube_GeoDt")
        mdb.outTable(rec_Cube_GeoDt)


        // ---
        // Внесем изменения
        println()
        println("----------")
        println("Внесем изменения, cube: " + cubeName)

        // GeoData
        double densityOil = 0.8 + Math.round(Math.random() * 2000) / 10000
        println("GeoData.densityOil: " + rec_GeoData.getDouble("densityOil") + " -> " + densityOil)
        mdb.execQuery("update GeoData set densityOil = :densityOil where id = :id", [
                id        : rec_GeoData.getLong("id"),
                densityOil: densityOil
        ])

        // GeoAttr
/*
        mdb.execQuery("update GeoAttr set id = id where id = :id", [
                id: rec_GeoAttr.getLong("id"),
        ])
*/

        //
        println()
    }


    @Override
    void checkChanges_after_Audit() {
        // ---
        StoreRecord rec_GeoData_1 = mdb.loadQuery("select * from GeoData where geo = :geo and GeoData.dbeg <= :dt and GeoData.dend > :dt limit 1", [
                geo: geo,
                dt : dt,
        ]).get(0)
        //
        StoreRecord rec_Cube_GeoDt_1 = mdb.loadQuery(sql_Cube_GeoDt(), [
                geo: geo,
                dt : dt,
        ]).get(0)

        //
        println("----------")
        println("После изменений")
        println()
        println("GeoData")
        mdb.outTable(rec_GeoData_1)
        println("Cube_GeoDt")
        mdb.outTable(rec_Cube_GeoDt_1)

        //
        assertEquals(rec_GeoData_1.getDouble("densityOil"), rec_Cube_GeoDt_1.getDouble("densityOil"), "Измененная densityOil не попала в Cube_GeoDt")
        assertEquals(false, rec_GeoData.getDouble("densityOil") == rec_GeoData_1.getDouble("densityOil"), "densityOil не была изменена в GeoData")
        assertEquals(false, rec_Cube_GeoDt.getDouble("densityOil") == rec_Cube_GeoDt_1.getDouble("densityOil"), "densityOil не была изменена в GeoData")
    }


    @Override
    CoordList getCoords_for_byOneCoord() {
        Store st = mdb.loadQuery("select * from geo where id in (1001,1006,1020) limit 3")
        List<Long> ids = CubeDataUtils.toListLong(st.getUniqueValues("id"))

        //
        CoordList coords = CoordList.create()
        for (Long id : ids) {
            Coord coord = Coord.create()
            coord.put("geo", id)
            coords.add(coord)
        }

        //
        return coords
    }


    String sql_Cube_GeoDt() {
        return """
select
    geo, dt, densityOil 
from 
    Cube_GeoDt
where
    geo = :geo and dt = :dt
"""
    }


}

