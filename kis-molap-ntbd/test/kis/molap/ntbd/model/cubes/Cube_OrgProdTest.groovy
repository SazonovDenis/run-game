package kis.molap.ntbd.model.cubes


import kis.molap.model.conf.*
import kis.molap.model.coord.*
import kis.molap.model.cube.*
import kis.molap.model.util.*
import kis.molap.model.value.*
import kis.molap.model.value.impl.*
import kis.molap.ntbd.model.base.*
import org.junit.jupiter.api.*

import static org.junit.jupiter.api.Assertions.*

class Cube_OrgProdTest extends CubeBase_Test {

    @Test
    void convertCoordsAndCalcTest() throws Exception {

        // Апдейтим liquidprod тем самым загрязняем cube_WellProd и получаем загрязненные координаты coords_WellProd
        mdb.execQuery("update liquidprod set val = 888 where well = 1031 and dbeg <= '2023-09-22' and dend > '2023-09-22';")
        var cube_WellProd = createCube("Cube_WellProd", mdb)
        CoordList coords_WellProd = cube_WellProd.getDirtyCoords(0, Long.MAX_VALUE)

        // Так как cube_WellProd был загрязнен, тем самым загрязнился cube_OrgProd с нужной для нас org
        // Получаем загрязненные координаты coords_OrgProd для org которую будем загрязнять
        var cube_OrgProd = createCube("Cube_OrgProd_Direct", mdb)
        CoordList coords_OrgProd = cube_OrgProd.getDirtyCoords(0, Long.MAX_VALUE)
        cube_OrgProd.convertCoords("Cube_WellProd", coords_WellProd, coords_OrgProd)
        assertEquals(true, coords_OrgProd.size() > 0, "Значения есть")

        // Апдейтим orgattr c целевой org в целях загрязнения cube_OrgProdParent
        mdb.execQuery("update orgattr set parent = 1001 where org = 1038 and dbeg <= '2023-09-22' and dend > '2023-09-22';")

        // Отлавливаем загрязнения cube_OrgProdParent и пересчитываем его
        var cube_OrgProdParent = createCube("Cube_OrgProd", mdb)
        CoordList coords_OrgProdParent = cube_OrgProdParent.getDirtyCoords(0, Long.MAX_VALUE)
        assertEquals(true, coords_OrgProdParent.size() > 0, "Значения есть")

        cube_OrgProdParent.convertCoords("cube_OrgProd_Direct", coords_OrgProd, coords_OrgProdParent)

        ICalcResultStream res = new CalcResultStreamArray()
        cube_OrgProdParent.calc(coords_OrgProdParent, ageMinDt, ageMaxDt, res)

    }

    void setUp() throws Exception {
        cubeName = "Cube_OrgProd"
        //
        super.setUp()
    }

    @Test
    void getDirtyCoords_Test() {
        cubeName = "Cube_OrgProd"
        println("cube: " + cubeName)

        // Внесем изменения
        mdb.updateRec("orgAttr", [id: 5308, org: 1308])

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
    void calcTest() throws Exception {
        ICalcResultStream iCalcResultStream = new CalcResultStreamArray()

        ICalcData cube = createCube()

        cube.calc(null, intervalDbeg, intervalDend, iCalcResultStream)

    }
}
