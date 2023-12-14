package kis.molap.ntbd.model

import kis.molap.model.conf.*
import kis.molap.model.cube.*
import kis.molap.model.value.impl.*
import kis.molap.model.worker.impl.*
import kis.molap.ntbd.model.base.*
import org.junit.jupiter.api.*

public class CsvAll_Test extends MolapBase_Test {


    /**
     * Расчитывает все кубы интервально без MoWorker, результат записывает в БД через промежуточный Csv.
     */
    @Test
    public void intervalAll_noWorker() throws Exception {
        sw.start("total")

        //
        for (CubeInfo cubeInfo : cubeService.getCubes()) {
            String cubeName = cubeInfo.getName()
            println("cube: " + cubeName)

            //
            calcToCsvWriteToDB(cubeName)

            //
            println()
        }

        //
        sw.stop("total")
        sw.printItems()

        //
        printTestSelect()
    }


    /**
     * Расчитывает все кубы интервально без MoWorker, результат помещает в Csv.
     */
    @Test
    public void byInterval_toCsv() throws Exception {
        sw.start("total")

        //
        for (CubeInfo cubeInfo : cubeService.getCubes()) {
            String cubeName = cubeInfo.getName()
            println("cube: " + cubeName)

            //
            calcToCsv(cubeName)

            //
            println()
        }

        //
        sw.stop("total")
        sw.printItems()

        //
        printTestSelect()
    }


    void calcToCsv(String cubeName) {
        sw.start(cubeName)

        // Создаем куб
        ICalcData cube = createCube(cubeName, mdbRead)

        // Результат в csv
        CubeInfo cubeInfo = createCubeInfo(cubeName)
        SpaceInfo spaceInfo = createSpaceInfo(cubeInfo.getSpace())
        CalcResultStreamFileCsv res = new CalcResultStreamFileCsv(cubeInfo, spaceInfo)
        res.file = new File("./temp/" + cubeName + ".csv")
        res.doHeaders = true
        res.open()

        // Пересчет
        mdbRead.startTran()
        try {
            cube.calc(null, intervalDbeg, intervalDend, res)
        } finally {
            mdbRead.commit()
        }

        //
        res.close()
        println("res: " + res)

        //
        sw.stop(cubeName)
    }


    void calcToCsvWriteToDB(String cubeName) {
        sw.start(cubeName)

        // Результат будет в csv-файл
        CalcResultStreamFileCsv resCsv = new CalcResultStreamFileCsv(cubeInfo, spaceInfo)
        String tempDir = System.getProperty("java.io.tmpdir")
        //String tempDir = "temp/"
        resCsv.delimiter = "\t"
        resCsv.doHeaders = false
        resCsv.file = new File(tempDir + "/" + cubeName + ".csv")
        CubeInfo cubeInfo = createCubeInfo(cubeName)
        resCsv.open()

        //
        ICalcData cube = createCube(cubeName, mdbRead)

        // Пересчет
        mdbRead.startTran()
        try {
            cube.calc(null, intervalDbeg, intervalDend, resCsv)
        } finally {
            mdbRead.commit()
        }

        //
        resCsv.close()
        println("res: " + resCsv)

        // Запись csv в БД.
        // Для пероначального заполнения можно использовать insertFromCsv, а при обновлении - insertOrUpdateFromCsv.
        WriterDb writer = new WriterDb(mdbWrite)
        //writer.insertFromCsv(cubeInfo, resCsv.file)
        writer.insertOrUpdateFromCsv(cubeInfo, resCsv.file)

        //
        sw.stop(cubeName)
    }


}

