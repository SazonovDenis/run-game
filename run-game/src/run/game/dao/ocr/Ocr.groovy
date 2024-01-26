package run.game.dao.ocr

import jandcode.commons.*
import jandcode.commons.error.*
import jandcode.commons.process.*
import jandcode.core.dao.*
import run.game.dao.*
import run.game.testdata.fixture.*

class Ocr extends RgmMdbUtils {


    @DaoMethod
    public List parseStill(String imgBase64) {
        long idUsr = getCurrentUserId()

        //
        int pos = imgBase64.indexOf(",") + 1
        String imgStr = imgBase64.substring(pos)
        byte[] img = UtString.decodeBase64(imgStr)

        //
        File inFile = File.createTempFile("rgm", ".png")
        FileOutputStream strm = new FileOutputStream(inFile);
        strm.write(img)
        strm.close()

        //
        String text = tesseract(inFile.getAbsolutePath(), "eng+rus")

        //
        //inFile.delete()

        // Частота встречаемости eng
        String dirBase = "data/web-grab/"
        Map<String, Integer> wordFrequencyMap_eng = ItemFact_fb.loadWordFrequencyMap(dirBase + "eng_top-50000.txt")

        List res = new ArrayList()

        //////////////////////////
        res.add(text)
        res.add("\r\n")
        res.add("===================")
        res.add("\r\n")
        //////////////////////////

        //
        String[] textArr = text.split(" ")
        for (String word : textArr) {
            word = word.toLowerCase().trim()
            if (word.length() > 1 && wordFrequencyMap_eng.containsKey(word)) {
                res.add(word)
            }
        }

        //
        return res
    }

    String tesseract(String inFileName, String lang) {
        String outFileName = UtFile.removeExt(inFileName)

        //
        String exeFile = "tesseract ${inFileName} ${outFileName} -l ${lang} --tessdata-dir /usr/local/share/tessdata/"

        //
        RunCmd runCmd = new RunCmd()
        runCmd.setShowout(false)
        runCmd.setSaveout(true)
        runCmd.setCmd(exeFile)
        runCmd.run()
        if (runCmd.getExitCode() > 0) {
            String r = UtString.join(runCmd.getOut(), "\n")
            throw new XError("error in {0}:\n{1}", exeFile, r)
        }

        //
        outFileName = outFileName + ".txt"
        String text = UtFile.loadString(outFileName)

        //
        //new File(outFileName).delete()

        //
        return text
    }


}
