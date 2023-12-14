package kis.molap.ntbd.model.cubes

import jandcode.commons.datetime.XDate
import kis.molap.model.coord.Coord
import kis.molap.model.coord.CoordList
import kis.molap.model.cube.ICalcData
import kis.molap.model.util.UtMolapPrint
import kis.molap.model.value.ICalcResultStream
import kis.molap.model.value.impl.CalcResultStreamArray
import kis.molap.ntbd.model.base.CubeBase_Test
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertEquals

class Cube_OrgProd_WaterProd_Direct_Test extends CubeBase_Test{

    void setUp() throws Exception {
        cubeName = "Cube_OrgProd_WaterProd"
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

}
