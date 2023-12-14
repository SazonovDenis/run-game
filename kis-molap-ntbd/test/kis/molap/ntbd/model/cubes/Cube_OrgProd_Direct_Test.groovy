package kis.molap.ntbd.model.cubes

import jandcode.commons.datetime.*
import kis.molap.model.conf.*
import kis.molap.model.coord.*
import kis.molap.model.cube.*
import kis.molap.model.util.*
import kis.molap.model.value.*
import kis.molap.model.value.impl.*
import kis.molap.ntbd.model.base.*
import org.junit.jupiter.api.*

import static org.junit.jupiter.api.Assertions.*

class Cube_OrgProd_Direct_Test extends CubeBase_Test {

    void setUp() throws Exception {
        cubeName = "Cube_OrgProd_Direct"
        //
        super.setUp()
    }

    @Test
    void byOneCoord() throws Exception {
        println("cube: " + cubeName)

        intervalDbeg = ageMinDt
        intervalDend = ageMaxDt

        // Создаем куб
        ICalcData cube = createCube()

        // Результат
        ICalcResultStream res = new CalcResultStreamArray()

        // Что считаем
        CoordList coords = CoordList.create()
        Coord coord = Coord.create()
        coord.put("org", 1095)
        coord.put("dt", XDate.create("2023-03-01"))
        coords.add(coord)

        println()
        println("coords:")
        UtMolapPrint.printCoordList(coords, 5)

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


    @Test
    void getDirtyCoords_Test() {
        println("cube: " + cubeName)

        // Внесем изменения
        mdb.updateRec("wellOrg", [id: 4004, well: 1001])

        // Создаем куб
        Cube cube = createCube()
        CubeInfo cubeInfo = createCubeInfo()

        // Узнаем интервал загрязненных координат
        long auditAgeFrom = ageManager.getCubeAgeDone(cubeInfo)
        long auditAgeTo = ageManager.getActualAge()
        println("auditAgeFrom: " + auditAgeFrom + ", auditAgeTo: " + auditAgeTo)

        //
        CoordList list = cube.getDirtyCoords(auditAgeFrom, auditAgeTo)

        //
        UtMolapPrint.printCoordList(list, 5)

    }

    @Test
    void convertCoordsTest() throws Exception {

        var cube_WellProd = createCube("Cube_WellProd", mdb)
        var cube_OrgProd = createCube("Cube_OrgProd_Direct", mdb)

        CoordList coords_WellProd = cube_WellProd.getDirtyCoords(0, Long.MAX_VALUE)

        //assertEquals(true, coords_WellProd.size() > 0, "Значения есть")

        mdb.execQuery("update liquidprod set val = 888 where well = 1001 and dbeg <= '2023-09-01' and dend > '2023-09-01';")

        coords_WellProd = cube_WellProd.getDirtyCoords(0, Long.MAX_VALUE)

        assertEquals(true, coords_WellProd.size() > 0, "Значения есть")

        CoordList coords_OrgProd = cube_OrgProd.getDirtyCoords(0, Long.MAX_VALUE)

        cube_OrgProd.convertCoords("Cube_WellProd", coords_WellProd, coords_OrgProd)

    }


}
