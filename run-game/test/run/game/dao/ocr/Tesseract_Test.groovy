package run.game.dao.ocr

import jandcode.commons.*
import jandcode.commons.error.*
import jandcode.commons.process.*
import jandcode.commons.stopwatch.DefaultStopwatch
import jandcode.commons.stopwatch.Stopwatch
import org.junit.jupiter.api.*
import run.game.dao.*

class Tesseract_Test extends RgmBase_Test {

    String inFileName = ""
    String outFileName = ""
    String outFileNameCL = ""
    String lang = "eng"
    String outFormat = ""


    @Test
    void t11() {
        inFileName = "test/run/game/dao/ocr/11-00.jpg"
        outFileName = "temp/11-00.txt"
        outFileNameCL = "temp/11-00"
        lang = "eng+rus"
        t()

        println()
        println("-------------------")
        println()

        inFileName = "test/run/game/dao/ocr/11-90.jpg"
        outFileName = "temp/11-90.txt"
        outFileNameCL = "temp/11-90"
        lang = "eng+rus"
        t()
    }

    @Test
    void t_seismic() {
        inFileName = "test/run/game/dao/ocr/seismic.jpg"
        outFileName = "temp/seismic.tsv"
        outFileNameCL = "temp/seismic"
        lang = "eng+rus"
        outFormat = "tsv"
        t()
    }

    void t() {
        Stopwatch sw = new DefaultStopwatch("ocr.parseStill")
        sw.start()

        //
        String exeFile = "tesseract ${inFileName} ${outFileNameCL} -l ${lang} ${outFormat} --tessdata-dir /usr/local/share/tessdata/"

        //
        RunCmd runCmd = new RunCmd()
        //runCmd.setDir(getApp().getAppdir())
        runCmd.setShowout(false)
        runCmd.setSaveout(true)
        runCmd.setCmd(exeFile)
        runCmd.run()
        if (runCmd.getExitCode() > 0) {
            String r = UtString.join(runCmd.getOut(), "\n")
            throw new XError("error in {0}:\n{1}", exeFile, r)
        }

        //
        sw.stop()

        //
        println()
        println("---")
        String text = UtFile.loadString(outFileName)
        println(text)
        println("---")

        //
        println(sw.toString())
    }

}
