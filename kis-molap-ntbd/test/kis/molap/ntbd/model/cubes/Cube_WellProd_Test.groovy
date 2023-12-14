package kis.molap.ntbd.model.cubes

import jandcode.commons.datetime.*
import jandcode.core.store.*
import kis.molap.model.coord.*
import kis.molap.model.coord.impl.*
import kis.molap.model.cube.*
import kis.molap.model.util.*
import kis.molap.model.value.*
import kis.molap.model.value.impl.*
import kis.molap.ntbd.model.*
import kis.molap.ntbd.model.base.*
import org.junit.jupiter.api.*

import static org.junit.jupiter.api.Assertions.*

public class Cube_WellProd_Test extends CubeBase_Test {


    void setUp() throws Exception {
        cubeName = "Cube_WellProd"
        //
        super.setUp()
        //
        //utils.logOn()
    }

    @Test
    @Disabled
    void byOneCoord_x() throws Exception {
        println("cube: " + cubeName)

        // Создаем куб
        ICalcData cube = createCube()

        // Результат
        ICalcResultStream res = new CalcResultStreamArray()

        // Что считаем
        Coord coord = new CoordImpl()
        coord.put("well", 1041)
        //
        CoordList coords = new CoordListImpl()
        coords.add(coord)
        println()
        println("coords:")
        UtMolapPrint.printCoordList(coords, 5)

        //
        XDate intervalDbeg = XDate.create("2022-01-20")
        XDate intervalDend = XDate.create("2022-01-20")

        // Пересчет
        cube.calc(coords, intervalDbeg, intervalDend, res)

        //
        println()
        println("values:")
        UtMolapPrint.printValuesList(res, 10)

        // Проверим, что на каждую координату только одно значение за каждый день
        assertEquals(true, res.size() > 0, "Значения есть")
        if (coords != null) {
            assertEquals(coords.size() * (intervalDend.diffDays(intervalDbeg) + 1), res.size(), "Количество значений")
        }
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

    @Override
    CoordList getCoords_for_byOneCoord() {
        Store st = mdb.loadQuery("select * from Well where id > 1000 limit 2")
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

