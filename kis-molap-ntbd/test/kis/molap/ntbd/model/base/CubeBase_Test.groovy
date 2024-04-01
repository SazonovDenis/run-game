package kis.molap.ntbd.model.base

import kis.molap.model.conf.*
import kis.molap.model.coord.*
import kis.molap.model.coord.impl.*
import kis.molap.model.cube.*
import kis.molap.model.util.*
import kis.molap.model.value.*
import kis.molap.model.value.impl.*
import kis.molap.model.worker.impl.*
import org.junit.jupiter.api.*

import static org.junit.jupiter.api.Assertions.*

/**
 * Проверка стандартного набора методов куба.
 * Для конкретного куба нужно просто унаследоваться и переопределить cubeName.
 */
public class CubeBase_Test extends MolapBase_Test {


    String cubeName


    Cube createCube() {
        return createCube(cubeName, mdbRead)
    }

    CubeInfo createCubeInfo() {
        return createCubeInfo(cubeName)
    }

    Space createSpace() {
        SpaceInfo spaceInfo = createSpaceInfo()
        return createSpace(spaceInfo.getName(), mdbRead)
    }

    SpaceInfo createSpaceInfo() {
        CubeInfo cubeInfo = createCubeInfo(cubeName)
        return createSpaceInfo(cubeInfo.getSpace())
    }


    @Override
    void setUp() throws Exception {
        super.setUp()

        // Расширим границы куба, чтобы тест на аудит работал без необходимости предварительно заполнять кубы,
        // ведь расчет по аудиту проверяет, что координата внутри границ куба.
        CubeInfo cubeInfo = createCubeInfo()
        ageManager.setMinDt(cubeInfo, ageMinDt)
        ageManager.setMaxDt(cubeInfo, ageMaxDt)
    }


    @Test
    void byOneCoord() throws Exception {
        println("cube: " + cubeName)

        // Создаем куб
        ICalcData cube = createCube()

        // Результат
        ICalcResultStream res = new CalcResultStreamArray()

        // Что считаем
        CoordList coords = getCoords_for_byOneCoord()
        println()
        println("coords:")
        UtMolapPrint.printCoordList(coords, 5)

        // Пересчет
        cube.calc(coords, intervalDbeg, intervalDend, res)

        //
        println()
        println("values:")
        UtMolapPrint.printValuesList(res, 10)

        // Проверим, что значения есть
        assertEquals(true, res.size() > 0, "Значения есть")

        // Проверим, что на каждую координату только одно значение за каждый день
        //if (coords != null) {
        //    assertEquals(coords.size() * (intervalDend.diffDays(intervalDbeg) + 1), res.size(), "Количество значений")
        //}
    }


    @Test
    public void byInterval_Worker() throws Exception {
        println("cube: " + cubeName)

        // Считаем куб для интервала дат
        worker.calcIntervalCube(cubeName, intervalDbeg, intervalDend, false)
    }


    @Test
    public void byInterval_noWorker() throws Exception {
        println("cube: " + cubeName)

        // Создаем куб
        Cube cube = createCube()
        Space space = createSpace()
        SpaceInfo spaceInfo = createSpaceInfo()

        // Результат
        ICalcResultStream res = new CalcResultStreamFileCsv(cube.getInfo(), spaceInfo)
        res.open()

        // Считаем куб для интервала дат
        MolapStopWatch sw = new MolapStopWatch();
        sw.start("calc")

        ((ICalcData) cube).calc(null, intervalDbeg, intervalDend, res)

        sw.setValuePlus("calc", "count", res.size())
        sw.stop("calc")

        // Печать статистики
        sw.printItems()

        //
        UtMolapPrint.printValuesList(res.getBuffer(), 5)
        res.close()
        println("res: " + res)

        // // Проверим, что на каждый день только одно значение за каждый день
        // CoordList coords = space.getCoordsForInterval(intervalDbeg, intervalDend)
        // println("coords.size: " + coords.size() + ", days: " + (intervalDend.diffDays(intervalDbeg) + 1))
        // assertEquals(coords.size() * (intervalDend.diffDays(intervalDbeg) + 1), res.size(), "Количество значений")
    }


    @Test
    public void byAudit_Worker() throws Exception {
        println("----------")
        println("Обновление по аудиту")
        println("cube: " + cubeName)

        // Внесем изменения
        makeChanges_for_Audit()

        // Создаем куб
        CubeInfo cubeInfo = createCubeInfo()

        // Узнаем интервал загрязненных координат
        long auditAgeFrom = ageManager.getCubeAgeDone(cubeInfo)
        long auditAgeTo = ageManager.getActualAge()
        println("auditAgeFrom: " + auditAgeFrom + ", auditAgeTo: " + auditAgeTo)

        // Считаем куб по журналу
        worker.calcAuditCube(cubeName, auditAgeFrom, auditAgeTo, false)

        // Проверяем изменения после расчета по аудиту
        checkChanges_after_Audit()
    }


    @Test
    public void byAudit_NoWorker() throws Exception {
        println("----------")
        println("Обновление по аудиту")
        println("cube: " + cubeName)

        // Внесем изменения
        makeChanges_for_Audit()

        // Создаем куб
        Cube cube = createCube()
        CubeInfo cubeInfo = createCubeInfo()

        // Результат
        ICalcResultStream res = new CalcResultStreamArray()

        // Узнаем интервал загрязненных координат
        long auditAgeFrom = ageManager.getCubeAgeDone(cubeInfo)
        long auditAgeTo = ageManager.getActualAge()
        println("auditAgeFrom: " + auditAgeFrom + ", auditAgeTo: " + auditAgeTo)

        // Получим загрязненные координаты
        CoordList list = ((WorkerImpl) worker).getDirtyCoordsRecursive(cube, auditAgeFrom, auditAgeTo, sw)

        // Считаем куб загрязненным координатам (по журналу)
        for (Coord coord : list) {
            CoordList coordsToCalc = CoordList.create()
            coordsToCalc.add(coord)
            for (Period coordPeriod : UtCoord.getValueDateIterator(coord)) {
                cube.calc(coordsToCalc, coordPeriod.dbeg, coordPeriod.dend, res)
            }
        }

        //
        UtMolapPrint.printValuesList((CalcResultStreamArray) res, 5)
        println("res: " + res)
    }


    @Test
    public void getDirtyCoords() throws Exception {
        println("cube: " + cubeName)

        // Внесем изменения
        makeChanges_for_Audit()

        // Создаем куб
        Cube cube = createCube()
        CubeInfo cubeInfo = createCubeInfo()

        // Узнаем интервал загрязненных координат
        long auditAgeFrom = ageManager.getCubeAgeDone(cubeInfo)
        long auditAgeTo = ageManager.getActualAge()
        println("auditAgeFrom: " + auditAgeFrom + ", auditAgeTo: " + auditAgeTo)

        // Получим загрязненные координаты
        CoordList list = cube.getDirtyCoords(auditAgeFrom, auditAgeTo)

        //
        UtMolapPrint.printCoordList(list, 5)
    }


    /**
     * Координаты для точечного пересчета методом {@link CubeBase_Test#byOneCoord()}
     */
    CoordList getCoords_for_byOneCoord() {
        return null
    }


    /**
     * Вносит изменения так, чтобы было что расчитывать по аудиту
     */
    void makeChanges_for_Audit() {
        return
    }


    /**
     * Проверяет изменения после расчета по аудиту
     */
    void checkChanges_after_Audit() {
        return
    }


}