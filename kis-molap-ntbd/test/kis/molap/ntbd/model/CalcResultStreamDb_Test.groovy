package kis.molap.ntbd.model

import jandcode.commons.datetime.*
import jandcode.core.store.*
import kis.molap.model.conf.*
import kis.molap.model.coord.*
import kis.molap.model.value.*
import kis.molap.model.value.impl.*
import kis.molap.ntbd.model.base.*
import org.junit.jupiter.api.*

import static org.junit.jupiter.api.Assertions.*

class CalcResultStreamDb_Test extends MolapBase_Test {


    /**
     * Проверяем формирование sql
     */
    @Test
    public void test_sql() throws Exception {
        String cubeName = "Cube_WorkedTime"
        CubeInfo cubeInfo = createCubeInfo(cubeName)

        //
        CalcResultStreamDb res = new CalcResultStreamDb(mdb, cubeInfo)
        res.open()

        //
        println()
        println(res.sqlInsert())
        println()
        println(res.sqlUpdate())
    }


    /**
     * Проверяем запись в БД
     */
    @Test
    public void test_write() throws Exception {
        utils.logOn()

        //
        String cubeName = "Cube_WorkedTime"
        CubeInfo cubeInfo = createCubeInfo(cubeName)
        SpaceInfo spaceInfo = createSpaceInfo(cubeInfo.getSpace())
        String tableName = spaceInfo.getTable()

        // Координата и значение
        Coord coord = Coord.create()
        coord.put("well", 1001)
        coord.put("dt", XDate.create("2022-01-01"))
        //
        ValueSingle value = ValueSingle.create()
        value.put("workedTime", 23.5)

        // Удаляем точку из куба и проверяем, что её нет
        mdb.execQuery("delete from " + tableName + " where well = :well and dt = :dt", coord.getValues())
        Store st = mdb.loadQuery("select * from " + tableName + " where well = :well and dt = :dt", coord.getValues())
        mdb.outTable(st)
        assertEquals(0, st.size())

        // Формируем результат
        CalcResult rec = new CalcResult()
        rec.coord = coord
        rec.value = value

        // Открываем поток, помещаем значение в поток, закрываем
        CalcResultStreamDb res = new CalcResultStreamDb(mdb, cubeInfo)
        res.open()
        //
        res.addValue(rec)
        //
        res.close()

        // Проверяем, что теперь точка в кубе есть
        st = mdb.loadQuery("select * from " + tableName + " where well = :well and dt = :dt", coord.getValues())
        mdb.outTable(st)
        assertEquals(1, st.size())
        assertEquals(23.5, st.get(0).getDouble("workedTime"))
        assertEquals(1001, st.get(0).getLong("well"))
        assertEquals(XDate.create("2022-01-01"), st.get(0).getDate("dt"))
    }


}
