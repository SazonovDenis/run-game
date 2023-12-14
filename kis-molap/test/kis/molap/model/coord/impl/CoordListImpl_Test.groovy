package kis.molap.model.coord.impl

import jandcode.commons.datetime.*
import jandcode.commons.test.*
import kis.molap.model.coord.*
import kis.molap.model.util.*
import org.junit.jupiter.api.*

import static org.junit.jupiter.api.Assertions.*

class CoordListImpl_Test extends Base_Test {

    int test_speed_addPortionSize = 100000
    int test_speed_steps = 10

    /**
     * Проверки на деградацию производительности нашей реализации
     */
    @Test
    void test_speed() {
        CoordList list = CoordList.create()

        MolapStopWatch sw = new MolapStopWatch()

        for (int i = 0; i < test_speed_steps; i++) {
            println("step = " + i + ", list.size() = " + list.size())
            sw.start("xxx")

            //
            for (int j = 0; j < test_speed_addPortionSize; j++) {
                Coord coord = Coord.create()

                coord.put("i", i)
                coord.put("j", j)

                list.add(coord)
            }

            //
            sw.stop("xxx")
            sw.setValue("xxx", "addPortionSize", test_speed_addPortionSize)
            sw.printItems()
        }
    }

    /**
     * Проверки на деградацию производительности при реализации через HashSet
     */
    @Test
    void test_speed_hash_set() {
        HashSet<Coord> list = new HashSet<Coord>()

        MolapStopWatch sw = new MolapStopWatch()

        for (int i = 0; i < test_speed_steps; i++) {
            println("step = " + i + ", list.size() = " + list.size())
            sw.start("xxx")

            //
            for (int j = 0; j < test_speed_addPortionSize; j++) {
                Coord coord = Coord.create()

                coord.put("i", i)
                coord.put("j", j)

                list.add(coord)
            }

            //
            sw.stop("xxx")
            sw.setValue("xxx", "test_speed_addPortionSize", test_speed_addPortionSize)
            sw.printItems()
        }
    }

    @Test
    public void test_merge_dt() throws Exception {
        test_merge(testDt)
    }

    @Test
    public void test_merge_period() throws Exception {
        test_merge(testPeriod)
    }

    @Test
    public void test_merge_periodDt() throws Exception {
        test_merge(testPeriodDt)
    }


    def testDt = [
            "2017-03-02", "2017-03-02",

            // Поглощение даты
            "2017-03-02", "2017-03-02",

            // Поглощение даты с расширением
            "2017-03-03", "2017-03-02..2017-03-03,",
            "2017-03-04", "2017-03-02..2017-03-04,",

            // Поглощение даты
            "2017-03-02", "2017-03-02..2017-03-04,",
            "2017-03-03", "2017-03-02..2017-03-04,",
            "2017-03-04", "2017-03-02..2017-03-04,",

            // Поглощение даты с расширением
            "2017-03-01", "2017-03-01..2017-03-04,",
            "2017-03-02", "2017-03-01..2017-03-04,",
            "2017-03-03", "2017-03-01..2017-03-04,",
            "2017-03-04", "2017-03-01..2017-03-04,",
            "2017-03-05", "2017-03-01..2017-03-05,",

            // Добавление периодов
            "2017-04-05", "2017-03-01..2017-03-05,2017-04-05..2017-04-05",
            "2017-04-06", "2017-03-01..2017-03-05,2017-04-05..2017-04-06",
            "2017-04-07", "2017-03-01..2017-03-05,2017-04-05..2017-04-07",
    ]

    def testPeriod = [
            "2017-03-02..2017-03-03", "2017-03-02..2017-03-03",

            // Поглощение даты с расширением
            "2017-03-01", "2017-03-01..2017-03-03,",
            "2017-03-02", "2017-03-01..2017-03-03,",
            "2017-03-03", "2017-03-01..2017-03-03,",
            "2017-03-04", "2017-03-01..2017-03-04,",
            "2017-03-05", "2017-03-01..2017-03-05,",

            // Добавление периода
            "2017-04-02..2017-04-05", "2017-03-01..2017-03-05,2017-04-02..2017-04-05",

            // Поглощение периода
            "2017-04-02..2017-04-05", "2017-03-01..2017-03-05,2017-04-02..2017-04-05",
            "2017-04-02..2017-04-04", "2017-03-01..2017-03-05,2017-04-02..2017-04-05",
            "2017-04-03..2017-04-05", "2017-03-01..2017-03-05,2017-04-02..2017-04-05",
            "2017-04-03..2017-04-04", "2017-03-01..2017-03-05,2017-04-02..2017-04-05",

            // Поглощение периода с расширением
            "2017-04-01..2017-04-07", "2017-03-01..2017-03-05,2017-04-01..2017-04-07",
            "2017-04-02..2017-04-10", "2017-03-01..2017-03-05,2017-04-01..2017-04-10",
            "2017-04-01..2017-04-15", "2017-03-01..2017-03-05,2017-04-01..2017-04-15",

            // Добавление периода с объединением периодов
            "2017-03-02..2017-04-07", "2017-03-01..2017-04-15,",
    ]

    def testPeriodDt = [
            "2017-03-02", "2017-03-02",
            "2017-03-04", "2017-03-02..2017-03-02,2017-03-04..2017-03-04",

            // Поглощение даты  с объединением периодов
            "2017-03-03", "2017-03-02..2017-03-04,",
    ]

    void test_merge(List<String> testArr) throws Exception {
        CoordList coordList = CoordList.create()
        for (int i = 0; i < testArr.size(); i = i + 2) {
            println(testArr[i] + " -> " + testArr[i + 1])
            //
            Object valueAdd = parseCoordValue(testArr[i])
            Object resultCheck = parseCoordValue(testArr[i + 1])
            //
            Coord coord = Coord.create()
            coord.put("well", 1000)
            coord.put("dt", valueAdd)
            coordList.add(coord)
            //
            Coord coordCheck = coordList.get(coord)
            //
            assertEquals(resultCheck, coordCheck.get("dt"))
        }
    }

    Object parseCoordValue(String valueStr) {
        Object value;

        //
        if (valueStr.contains(",")) {
            value = new PeriodList()
            String[] arr1 = valueStr.split(",")
            for (int i = 0; i < arr1.size(); i++) {
                Period valuePeriod = parsePeriod(arr1[i])
                value.add(valuePeriod)
            }

        } else if (valueStr.contains("..")) {
            value = parsePeriod(valueStr)

        } else {
            value = XDate.create(valueStr)
        }

        //
        return value
    }

    Period parsePeriod(String valueStr) {
        String[] arr = valueStr.split("\\.\\.")
        return new Period(XDate.create(arr[0]), XDate.create(arr[1]))
    }

    @Test
    public void period_equals() throws Exception {
        Period period_2017 = new Period(XDate.create("2017-01-01"), XDate.create("2017-12-31"));
        Period period_2018 = new Period(XDate.create("2018-01-01"), XDate.create("2018-12-31"));
        PeriodList periodList_1 = new PeriodList()
        periodList_1.add(period_2017)
        //
        PeriodList periodList_2 = new PeriodList()
        periodList_2.add(period_2018)

        //
        Coord c1 = Coord.create()
        c1.put("i", 0)
        c1.put("j", 1)
        c1.put("periodList", periodList_1)

        //
        Coord c2 = Coord.create()
        c2.put("j", 1)
        c2.put("i", 0)
        c2.put("periodList", periodList_2)

        //
        Coord c3 = Coord.create()
        c3.put("j", 1)
        c3.put("i", 2)
        //c3.put("periodList", periodList_2)

        //
        Coord c4 = Coord.create()
        c4.put("i", 0)
        c4.put("j", 1)
        c4.put("periodList", new PeriodList())

        //
        Coord c5 = Coord.create()
        c5.put("i", 0)
        c5.put("j", 1)
        c5.put("periodList", XDateTime.create("2017-01-01"))

        //
        Coord c6 = Coord.create()
        c6.put("i", 0)
        c6.put("j", 1)
        c6.put("periodList", XDateTime.create("2018-01-01"))

        // Проверим равенство hash
        assertEquals(c1.hashCode(), c2.hashCode())
        assertNotSame(c1.hashCode(), c3.hashCode())
        assertNotSame(c2.hashCode(), c3.hashCode())
        assertEquals(c1.hashCode(), c4.hashCode())
        assertNotSame(c1.hashCode(), c5.hashCode())
        assertNotSame(c2.hashCode(), c5.hashCode())
        assertNotSame(c3.hashCode(), c5.hashCode())
        assertNotSame(c4.hashCode(), c5.hashCode())
        assertNotSame(c5.hashCode(), c6.hashCode())

        // Проверим равенство объектов
        assertEquals(c1, c2)
        assertNotSame(c1, c3)
        assertNotSame(c2, c3)
        assertEquals(c1, c4)
        assertNotSame(c1, c5)
        assertNotSame(c2, c5)
        assertNotSame(c3, c5)
        assertNotSame(c4, c5)
        assertNotSame(c5, c6)

        // Проверим равенство с точки зрения HashSet
        HashSet<Coord> coordsHash = new HashSet<>()
        //
        coordsHash.add(c1)
        assertEquals(1, coordsHash.size())
        coordsHash.add(c2)
        assertEquals(1, coordsHash.size())
        coordsHash.add(c3)
        assertEquals(2, coordsHash.size())
        coordsHash.add(c4)
        assertEquals(2, coordsHash.size())
        coordsHash.add(c5)
        assertEquals(3, coordsHash.size())
        coordsHash.add(c6)
        assertEquals(4, coordsHash.size())

        // Проверим равенство с точки зрения CubeCoordList
        CoordList coordsList = CoordList.create()
        //
        coordsList.add(c1)
        assertEquals(1, coordsList.size())
        coordsList.add(c2)
        assertEquals(1, coordsList.size())
        coordsList.add(c3)
        assertEquals(2, coordsList.size())
        coordsList.add(c4)
        assertEquals(2, coordsList.size())
        coordsList.add(c5)
        assertEquals(3, coordsList.size())
        coordsList.add(c6)
        assertEquals(4, coordsList.size())
    }

    /**
     * Проверки CubeCoordsList на правильность добавления совпадающих координат
     */
    @Test
    void test_indexOf() {
        CoordList list = CoordList.create()

        Coord coord0_1 = Coord.create()
        Coord coord0_2 = Coord.create()
        Coord coord2 = Coord.create()
        Coord coord3 = Coord.create()

        coord0_1.put("x1", 1234)
        coord0_1.put("x2", XDateTime.create("2017-01-01T23:55:12"))

        coord0_2.put("x1", 1234)
        coord0_2.put("x2", XDateTime.create("2017-01-01T23:55:12"))

        coord2.put("x1", 234567)
        coord2.put("x2", XDateTime.create("2017-01-01T23:55:12"))

        coord3.put("x1", 1234)
        coord3.put("x2", XDateTime.create("2000-12-22T22:55:12"))

        // --------------------------------
        // Пустой список
        assertEquals(0, list.size())

        // --------------------------------
        // Добавление значений
        list.add(coord0_1)

        //
        assertEquals(1, list.size())

        // --------------------------------
        // Повторное добавление той же координаты
        list.add(coord0_1)

        // Не должно приводить к росту списка
        assertEquals(1, list.size())

        // --------------------------------
        // Добавление эквивалентной координаты
        list.add(coord0_2)

        // Не должно приводить к росту списка
        assertEquals(1, list.size())

        // --------------------------------
        // Добавление другой координаты
        list.add(coord2)

        // Список растет
        assertEquals(2, list.size())

        // --------------------------------
        // Добавление другой координаты
        list.add(coord3)

        // Список растет
        assertEquals(3, list.size())
    }


}
