package run.game.dao.backstage

import jandcode.commons.*
import jandcode.commons.error.*
import jandcode.commons.process.*
import org.junit.jupiter.api.*
import run.game.dao.*

class Tesseract_Test extends RgmBase_Test {

    String inFileName = ""
    String outFileName = ""
    String outFileNameCL = ""
    String lang = "eng"

    @Test
    void t1() {
        inFileName = "test/run/game/dao/backstage/1.jpg"
        outFileName = "temp/1.txt"
        outFileNameCL = "temp/1"
        t()
    }

    @Test
    void t2() {
        inFileName = "test/run/game/dao/backstage/2.jpg"
        outFileName = "temp/2.txt"
        outFileNameCL = "temp/2"
        t()
    }

    @Test
    void t11() {
        inFileName = "test/run/game/dao/backstage/11-00.jpg"
        outFileName = "temp/11-0.txt"
        outFileNameCL = "temp/11-0"
        lang = "eng+rus"
        t()
        inFileName = "test/run/game/dao/backstage/11-90.jpg"
        outFileName = "temp/11-90.txt"
        outFileNameCL = "temp/11-90"
        lang = "eng+rus"
        t()
    }

    void t() {
        String exeFile = "tesseract ${inFileName} ${outFileNameCL} -l ${lang} --tessdata-dir /usr/local/share/tessdata/"

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

        println()
        println("---")
        String text = UtFile.loadString(outFileName)
        println(text)
        println("---")
    }

}
