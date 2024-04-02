package run.game.dao.ocr

import jandcode.commons.*
import jandcode.commons.error.*
import jandcode.commons.process.*
import jandcode.commons.stopwatch.DefaultStopwatch
import jandcode.commons.stopwatch.Stopwatch
import jandcode.core.dbm.std.DataBox
import jandcode.core.store.*
import org.apache.commons.io.*
import org.junit.jupiter.api.*
import run.game.dao.*
import run.game.dao.game.*

class Tesseract_tsv_Test extends RgmBase_Test {

    String inFileName = ""
    String outFileName = ""
    String outFileNameCL = ""
    String lang = "eng"
    String outFormat = ""

    @Test
    void t_11_00_tsv() {
        inFileName = "test/run/game/dao/ocr/11-00.jpg"
        outFileName = "temp/11-00.tsv"
        outFileNameCL = "temp/11-00"
        lang = "eng+rus"
        outFormat = "tsv"
        t()
    }

    @Test
    void t_11_00_srv_findStill() {
        //inFileName = "test/run/game/dao/ocr/11-00.jpg"
        //inFileName = "test/run/game/dao/ocr/orange.jpg"
        inFileName = "test/run/game/dao/ocr/rgm13528344128969128628.png"

        byte[] imgData = FileUtils.readFileToByteArray(new File(inFileName))
        String imgBase64 = UtString.encodeBase64(imgData)

        //
        ServerImpl srv = mdb.create(ServerImpl)
        DataBox res  = srv.findStill(imgBase64, 0)
        Store stFact = res.get("facts")
        Store positions = res.get("positions")

        //
        println ()
        println ("stFact")
        mdb.outTable(stFact)
        println ()
        println ("positions")
        mdb.outTable(positions)
    }

    @Test
    void t_11_00_ocr_parseStill() {
        inFileName = "test/run/game/dao/ocr/11-00.jpg"
        inFileName = "test/run/game/dao/ocr/orange.jpg"
        byte[] imgData = FileUtils.readFileToByteArray(new File(inFileName))
        String imgBase64 = UtString.encodeBase64(imgData)

        //
        Ocr ocr = mdb.create(Ocr)
        List listText = ocr.parseStill(imgBase64)

        //
        for (String s : listText) {
            println(s)
        }
    }

    @Test
    void t_11_00_ocr_parseStillMarkup() {
        inFileName = "test/run/game/dao/ocr/11-00.jpg"
        inFileName = "test/run/game/dao/ocr/orange.jpg"
        byte[] imgData = FileUtils.readFileToByteArray(new File(inFileName))
        String imgBase64 = UtString.encodeBase64(imgData)

        // OCR изображения
        Ocr ocr = mdb.create(Ocr)
        Store stText = ocr.parseStillMarkup(imgBase64)

        // Раскладываем результаты OCR на части
        List textList = ocr.getTextList(stText)
        Map<String, List> textMarkup = ocr.getTextMarkup(stText)

        //
        mdb.outTable(stText)

        println()
        println("---")

        //
        int count = 0
        for (String s : textList) {
            println(s)
            //
            count = count + 1
            if (count > 10) {
                break
            }
        }

        println()
        println("---")

        //
        count = 0
        for (String text : textMarkup.keySet()) {
            print(text + ": ")
            for (Map s1 : textMarkup.get(text)) {
                print(s1)
            }
            println()
            //
            count = count + 1
            if (count > 10) {
                break
            }
        }
    }


    @Test
    void t_book_tsv() {
        inFileName = "test/run/game/dao/ocr/book/IMG_20240218_223424.jpg"
        outFileName = "temp/IMG_20240218_223424.tsv"
        outFileNameCL = "temp/IMG_20240218_223424"
        lang = "eng+rus"
        outFormat = "tsv"
        t()
    }

    @Test
    void t_fls_tsv() {
        def fls = [
                "IMG-20240219-WA0000.jpg",
                "IMG-20240219-WA0001.jpg",
                "IMG_20240218_214457.jpg",
                "IMG_20240218_214511.jpg",
                "IMG_20240218_223424.jpg",
                "IMG_20240218_223449.jpg",
        ]

        for (String fileName : fls) {
            inFileName = "test/run/game/dao/ocr/book/" + fileName
            outFileName = "temp/" + fileName + ".txt"
            outFileNameCL = "temp/" + fileName
            lang = "eng+rus"
            //outFormat = "tsv"
            t()
        }
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
        println("outFileName: " + outFileName)
        String text = UtFile.loadString(outFileName)
        println(text)
        println("---")

        //
        println(sw.toString())
    }

}
