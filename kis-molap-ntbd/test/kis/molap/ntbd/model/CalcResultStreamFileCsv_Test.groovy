package kis.molap.ntbd.model

import jandcode.commons.datetime.*
import kis.molap.model.conf.*
import kis.molap.model.coord.*
import kis.molap.model.value.*
import kis.molap.model.value.impl.*
import kis.molap.ntbd.model.base.*
import org.junit.jupiter.api.*

class CalcResultStreamFileCsv_Test extends MolapBase_Test {


    @Test
    public void test_write() throws Exception {
        String cubeName = "Cube_WorkedTime"
        CubeInfo cubeInfo = createCubeInfo(cubeName)
        SpaceInfo spaceInfo = createSpaceInfo(cubeInfo.getSpace())

        // Координата и значение
        Coord coord = Coord.create()
        coord.put("well", 1001)
        coord.put("dt", XDate.create("2022-01-01"))
        //
        ValueSingle value = ValueSingle.create()
        value.put("workedTime", 23.5)

        // Формируем результат
        CalcResult rec = new CalcResult()
        rec.coord = coord
        rec.value = value

        // Пишем результат в csv-файл
        CalcResultStreamFileCsv res = new CalcResultStreamFileCsv(cubeInfo, spaceInfo)
        res.file = new File("temp/" + cubeName + ".csv")
        res.doHeaders = true
        res.open()
        //
        res.addValue(rec)
        //
        res.close()

        //
        println("res: " + res)
    }


}
