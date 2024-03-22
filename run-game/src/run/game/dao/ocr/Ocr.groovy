package run.game.dao.ocr

import jandcode.commons.*
import jandcode.commons.error.*
import jandcode.commons.process.*
import jandcode.commons.stopwatch.*
import jandcode.core.store.*
import org.slf4j.*
import run.game.dao.*
import run.game.util.*

class Ocr extends RgmMdbUtils {


    protected static Logger log = LoggerFactory.getLogger(Ocr.class)

    /**
     * Найти текст на картинке
     *
     * @param imgBase64 изображение
     * @return список найденных строк
     */
    public List parseStill(String imgBase64) {
        Stopwatch sw = new DefaultStopwatch("ocr.parseStill")
        sw.start()

        // Строку base64 скинем в файл
        File inFile = base64ToTmpFile(imgBase64)

        // Распарсим
        String outFileName = tesseract(inFile.getAbsolutePath(), "eng+rus", "")

        // Читаем ответ
        String text = UtFile.loadString(outFileName)

        // Удалим временные файлы после запуска tesseract
        if (!getApp().getEnv().isDev() && !getApp().getEnv().isTest()) {
            File outFile = new File(outFileName)
            outFile.delete()

            //
            inFile.delete()
        }

        // Вернем результат
        String[] itemsText = text.split()
        List res = itemsText.toList()

        //
        if (log.isInfoEnabled()) {
            log.info(sw.toString())
        }

        //
        return res
    }


    /**
     * Найти текст на картинке, его расположение
     * (и все прочие данные, которые выдает tesseract в формате tsv)
     *
     * @param imgBase64 изображение
     * @return найденные места с текстом
     */
    public Store parseStillMarkup(String imgBase64) {
        Stopwatch sw = new DefaultStopwatch("ocr.parseStill")
        sw.start()

        // Строку base64 скинем в файл
        File inFile = base64ToTmpFile(imgBase64)

        // Распарсим
        String outFileName = tesseract(inFile.getAbsolutePath(), "eng+rus", "tsv")

        // Читаем ответ
        Store stParsedText = mdb.createStore("Tesseract.tsv")
        RgmCsvUtils csvUtils = mdb.create(RgmCsvUtils)
        csvUtils.addFromCsv(stParsedText, outFileName)

        // Удалим временные файлы после запуска tesseract
        if (!getApp().getEnv().isDev() && !getApp().getEnv().isTest()) {
            File outFile = new File(outFileName)
            outFile.delete()

            //
            inFile.delete()
        }


        //
        if (log.isInfoEnabled()) {
            log.info(sw.toString())
        }

        //
        return stParsedText
    }


    public List getTextList(Store stText) {
        List res = new ArrayList()

        //
        for (StoreRecord rec : stText) {
            if (rec.getInt("conf") != -1) {
                String text = rec.getString("text")
                if (text.length() > 0) {
                    res.add(rec.getString("text"))
                }
            }
        }

        //
        return res
    }

    public Map<String, List> getTextMarkup(Store stText) {
        Map<String, List> res = new HashMap<>()

        //
        for (StoreRecord rec : stText) {
            if (rec.getInt("conf") != -1) {
                String text = rec.getString("text")
                //
                if (text.length() > 0) {
                    List textPositions = res.get(text)
                    if (textPositions == null) {
                        textPositions = new ArrayList()
                        res.put(text, textPositions)
                    }
                    //
                    Map positionInfo = new HashMap(rec.getValues())
                    textPositions.add(positionInfo)
                }
            }
        }

        //
        return res
    }

    String tesseract(String inFileName, String lang, String outFormat) {
        String outFileName = UtFile.removeExt(inFileName)

        // Для отладки
        if (getApp().getEnv().isDev() && !getApp().getEnv().isTest()) {
            inFileName = "run-game/test/run/game/dao/ocr/alice.jpg"
        }

        //
        String exeFile = "tesseract ${inFileName} ${outFileName} -l ${lang} ${outFormat} --tessdata-dir /usr/local/share/tessdata/"

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

        //
        if (outFormat.length() > 0) {
            outFileName = outFileName + "." + outFormat
        } else {
            outFileName = outFileName + ".txt"
        }

        //
        return outFileName
    }


    File base64ToTmpFile(String imgBase64) {
        // Поправим строку, котора пришла с web - в ней могут быть лишние части
        int pos = imgBase64.indexOf(",") + 1
        String imgStr = imgBase64.substring(pos)

        // Строку base64 в байты
        byte[] img = Base64.getMimeDecoder().decode(imgStr)

        // Скинем в файл
        File inFile = File.createTempFile("rgm", ".png")
        FileOutputStream strm = new FileOutputStream(inFile)
        strm.write(img)
        strm.close()

        //
        return inFile
    }

}
