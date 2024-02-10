package run.game.dao.ocr

import jandcode.commons.*
import jandcode.commons.error.*
import jandcode.commons.process.*
import run.game.dao.*

class Ocr extends RgmMdbUtils {


    public List parseStill(String imgBase64) {
        // Строку base64
        int pos = imgBase64.indexOf(",") + 1
        String imgStr = imgBase64.substring(pos)
        byte[] img = UtString.decodeBase64(imgStr)

        // Скинем в файл
        File inFile = File.createTempFile("rgm", ".png")
        FileOutputStream strm = new FileOutputStream(inFile);
        strm.write(img)
        strm.close()

        // Распарсим
        String text = tesseract(inFile.getAbsolutePath(), "eng+rus")

        //
        inFile.delete()

        // Вернем
        String[] itemsText = text.split()

        //
        return itemsText.toList()
    }


    String tesseract(String inFileName, String lang) {
        String outFileName = UtFile.removeExt(inFileName)

        //
        String exeFile = "tesseract ${inFileName} ${outFileName} -l ${lang} --tessdata-dir /usr/local/share/tessdata/"

        // Запускаем tesseract
        RunCmd runCmd = new RunCmd()
        runCmd.setShowout(false)
        runCmd.setSaveout(true)
        runCmd.setCmd(exeFile)
        runCmd.run()
        if (runCmd.getExitCode() > 0) {
            String r = UtString.join(runCmd.getOut(), "\n")
            throw new XError("error in {0}:\n{1}", exeFile, r)
        }

        // Читаем ответ
        outFileName = outFileName + ".txt"
        File outFile = new File(outFileName)
        String text = UtFile.loadString(outFileName)

        //
        outFile.delete()

        //
        return text
    }


}
