package run.game.testdata.fixture

import jandcode.commons.*
import jandcode.commons.error.*
import jandcode.core.dbm.fixture.*
import jandcode.core.store.*
import org.apache.commons.io.filefilter.*
import org.slf4j.*
import run.game.dao.*
import run.game.dao.backstage.*
import run.game.util.*

/**
 * Выполняет разбор и загрузку словарей: слова и их переводы, звуки.
 * Получаются сущности и факты, а также их тэги.
 */
class ItemFact_fb extends BaseFixtureBuilder {

    StoreIndex idxTagType
    StoreIndex idxFactType

    String dirBase = "data/web-grab/"
    String badCsv = "temp/bad-db.csv"

    long genIdItemTag = 1000000
    long genIdFactTag = 1000000

    Set<String> notFoundThing = new HashSet<>()

    def dirs = [
            "manual",
            "kz.kaz-tili.kz",
            "kz.sozdik.soyle.kz",
            "1000-all",
            "1000-englishdom",
            "1000-preply",
            "1000-puzzle-english",
            "1000-skyeng",
            "10000-kreekly",
            "200-puzzle-english",
            "5000-studynow",
            "8000-wordsteps",
            "level",
    ]

    def dirsOnlyNew = [
            "200-puzzle-english": true,     // всего двести, а шум в количестве переводов
            "5000-studynow"     : true,     // разные значения - через запятую, фиг разберешься
            "8000-wordsteps"    : true,     // разные значения - через запятую (очень часто!)
    ]

    def dirsDefaultWordFrequency = [
            "1000-englishdom"    : 1000,
            "1000-preply"        : 1000,
            "1000-puzzle-english": 1000,
            "1000-skyeng"        : 1000,
            "200-puzzle-english" : 200,
    ]

    // Уникальность значений
    Set<String> tagValueSet = new HashSet<>()
    Map<String, StoreRecord> itemsMap = new HashMap<>()

    protected void onBuild() {
        FixtureTable fxItem = fx.table("Item")
        FixtureTable fxFact = fx.table("Fact")
        FixtureTable fxItemTag = fx.table("ItemTag")
        FixtureTable fxFactTag = fx.table("FactTag")

        //
        Logger log = UtLog.getLogConsole()
        LogerFiltered logCube = new LogerFiltered(log)

        //
        RgmCsvUtils utils = mdb.create(RgmCsvUtils)

        //
        idxTagType = mdb.loadQuery("select * from TagType").getIndex("code")
        idxFactType = mdb.loadQuery("select * from FactType").getIndex("code")

        //
        Store stCsvBad = mdb.createStore("dat.csv.bad")

        // Частота встречаемости eng
        Map<String, Long> wordFrequencyMap_eng = loadWordFrequencyMap(dirBase + "eng_top-50000.txt")

        //
        long genIdItem = 1000000
        long genIdFact = 1000000

        //
        long idItem = 0
        String key

        //
        for (String dir : dirs) {
            // csv
            String[] filesCsvName = new File(dirBase + dir).list(new WildcardFileFilter("dat*.csv"))

            // mp3
            String[] dirsSoundArr = new File(dirBase + dir + "/mp3").list(new DirectoryFileFilter())
            // Ищем также и напрямую в каталоге dirBase + dir + "/mp3"
            List<String> dirsSound = null
            if (dirsSoundArr != null) {
                dirsSound = dirsSoundArr.toList()
                dirsSound.add("")
            }


            for (String fileCsvName : filesCsvName) {
                String fileCsv = dirBase + dir + "/" + fileCsvName

                //
                String wordLang_1 = "eng"
                String wordLang_2 = "rus"
                if (dir.equals("kz.kaz-tili.kz") ||
                        dir.equals("kz.sozdik.soyle.kz")) {
                    wordLang_1 = "kaz"
                }

                //
                println()
                println("fileCsv: " + fileCsv)

                // Заполним из наших csv
                Store stCsv = mdb.createStore("dat.csv")
                utils.addFromCsv(stCsv, fileCsv)
                //
                println("  size: " + stCsv.size())

                //
                logCube.logStepStart(stCsv.size())

                //
                Store stItem = fxItem.getStore()
                Store stItemTag = fxItemTag.getStore()
                Store stFact = fxFact.getStore()
                Store stFactTag = fxFactTag.getStore()

                //
                for (StoreRecord recCsv : stCsv) {

                    try {
                        String word_1 = recCsv.getString(wordLang_1)
                        word_1 = getWord(wordLang_1, word_1)
                        //
                        String word_2 = recCsv.getString(wordLang_2)
                        String[] word_2_arr = getWord_arr(wordLang_2, dir, word_2)

                        // Отсеиваем ошибки и мусор
                        validateWord(wordLang_1, word_1)
                        validateWord(wordLang_2, word_2_arr)

                        //
                        List<String> soundFilesArr = getSoundFiles(dirBase + dir + "/mp3/", dirsSound, word_1)

                        // Первый раз встретили слово?
                        StoreRecord recItem = itemsMap.get(word_1)

                        // Для плохих источников не пополняем переводы, берем только новые слова
                        boolean doAddTranslations = true
                        if (dirsOnlyNew.getOrDefault(dir, false) == true && recItem != null) {
                            //throw new XError("badDir, dir: " + dir + ", word: " + word_1 + ", wordLang: " + word_2_arr)
                            doAddTranslations = false
                        }

                        if (recItem == null) {
                            // Добавляем Item
                            recItem = stItem.add()
                            itemsMap.put(word_1, recItem)
                            //
                            genIdItem = genIdItem + 1
                            recItem.setValue("id", genIdItem)
                            recItem.setValue("value", word_1)
                            //
                            idItem = recItem.getLong("id")

                            // Добавляем ItemTag:word-lang
                            addItemTag(idItem, "word-lang", wordLang_1, stItemTag)

                            // Добавляем ItemTag:top-list (пробуем вычислить его автоматически)
                            String topList = getTopList(wordFrequencyMap_eng, dir, word_1)
                            if (!UtCnv.isEmpty(topList)) {
                                addItemTag(idItem, "top-list", topList, stItemTag)
                            }
                        } else {
                            idItem = recItem.getLong("id")
                        }

                        // Добавляем или обновляем ItemTag:top-list
                        String topList = recCsv.getString("top-list")
                        topList = topList.trim().toLowerCase()
                        if (!UtCnv.isEmpty(topList)) {
                            addItemTag(idItem, "top-list", topList, stItemTag)
                        }

                        // Добавляем ItemTag:word-category
                        for (int n = 1; n <= 3; n++) {
                            String category = recCsv.getString("category" + n)
                            category = category.trim().toLowerCase()
                            if (!UtCnv.isEmpty(category)) {
                                addItemTag(idItem, "word-category", category, stItemTag)
                            }
                        }

                        // Добавляем ItemTag:level-grade
                        String grade = recCsv.getString("grade")
                        grade = grade.trim().toLowerCase()
                        if (!UtCnv.isEmpty(grade)) {
                            addItemTag(idItem, "level-grade", grade, stItemTag)
                        }

                        // Добавляем Fact:word-spelling
                        key = idItem + "_word-spelling_" + word_1
                        if (!tagValueSet.contains(key)) {
                            tagValueSet.add(key)
                            // Добавляем Fact
                            genIdFact = genIdFact + 1
                            StoreRecord recFact = stFact.add()
                            recFact.setValue("id", genIdFact)
                            recFact.setValue("item", idItem)
                            recFact.setValue("factType", getFactType("word-spelling"))
                            recFact.setValue("value", word_1)
                            // Добавляем FactTag:word-lang
                            String wordLang = wordLang_1
                            addFactTag(genIdFact, "word-lang", wordLang, stFactTag)
                        }

                        // Добавляем Fact:word-transcription
                        String transcription = recCsv.getString("trans")
                        transcription = clearTranscription(transcription)
                        if (!UtCnv.isEmpty(transcription)) {
                            key = idItem + "_word-transcription_" + transcription
                            if (!tagValueSet.contains(key)) {
                                tagValueSet.add(key)
                                //
                                genIdFact = genIdFact + 1
                                StoreRecord recFact_3 = stFact.add()
                                recFact_3.setValue("id", genIdFact)
                                recFact_3.setValue("item", idItem)
                                recFact_3.setValue("factType", getFactType("word-transcription"))
                                recFact_3.setValue("value", transcription)
                            }
                        }

                        // Добавляем Fact:word-translate
                        if (doAddTranslations) {
                            for (String translate : word_2_arr) {
                                if (!UtCnv.isEmpty(translate)) {
                                    key = idItem + "_word-translate_" + translate
                                    if (!tagValueSet.contains(key)) {
                                        tagValueSet.add(key)
                                        // Добавляем Fact
                                        genIdFact = genIdFact + 1
                                        StoreRecord recFact_1 = stFact.add()
                                        recFact_1.setValue("id", genIdFact)
                                        recFact_1.setValue("item", idItem)
                                        recFact_1.setValue("factType", getFactType("word-translate"))
                                        recFact_1.setValue("value", translate)
                                        // Добавляем FactTag
                                        String direction = wordLang_1 + "-" + wordLang_2
                                        addFactTag(genIdFact, "word-translate-direction", direction, stFactTag)
                                        // Добавляем FactTag
                                        addFactTag(genIdFact, "dictionary", RgmDbConst.TagValue_dictionary_base, stFactTag)
                                    }
                                }
                            }
                        }

                        // Добавляем Fact:word-sound
                        for (String soundFile : soundFilesArr) {
                            if (!UtCnv.isEmpty(soundFile)) {
                                key = idItem + "_word-sound_" + soundFile
                                if (!tagValueSet.contains(key)) {
                                    tagValueSet.add(key)
                                    // Добавляем Fact:word-sound
                                    genIdFact = genIdFact + 1
                                    StoreRecord recFact_1 = stFact.add()
                                    recFact_1.setValue("id", genIdFact)
                                    recFact_1.setValue("item", idItem)
                                    recFact_1.setValue("factType", getFactType("word-sound"))
                                    String soundFileValue = soundFile.substring(dirBase.length())
                                    recFact_1.setValue("value", soundFileValue)
                                    // Добавляем FactTag:word-sound-info
                                    String[] soundSourceArr = soundFile.split("/")
                                    if (!soundSourceArr[soundSourceArr.length - 2].equals("mp3")) {
                                        String soundSource = soundSourceArr[soundSourceArr.length - 2]
                                        soundSource = soundSource.trim().toLowerCase()
                                        //
                                        addFactTag(genIdFact, "word-sound-info", soundSource, stFactTag)
                                    }
                                }
                            }
                        }

                        //
                        logCube.logStepStep()
                    }
                    catch (Exception e) {
                        StoreRecord recCsvBad = stCsvBad.add(recCsv.getValues())
                        recCsvBad.setValue("error", e.getMessage())
                        recCsvBad.setValue("file", fileCsv)
                        if (!e.getMessage().contains("isAlphasEng") && !e.getMessage().contains("isAlphasKaz") && !e.getMessage().contains("isAlphasRus")) {
                            println(e.message)
                        }
                    }
                }
            }
        }

        //
        println()
        println("All dirs done")


        //
        utils.saveToCsv(stCsvBad, new File(badCsv))

        //
        println()
        println("stCsvBad.size: " + stCsvBad.size())
    }

    void validateEng(String eng) {
        if (UtCnv.isEmpty(eng)) {
            throw new XError("validate eng, isEmpty")
        }
        if (!UtWord.isAlphasEng(eng)) {
            throw new XError("validate eng, isAlphasEng: " + eng)
        }
    }


    void validateRus(String rus) {
        if (UtCnv.isEmpty(rus)) {
            //throw new XError("validate rus, isEmpty")
        }
        if (!UtWord.isAlphasRus(rus)) {
            throw new XError("validate rus, isAlphasRus: " + rus)
        }
    }

    void validateKaz(String kaz) {
        if (UtCnv.isEmpty(kaz)) {
            //throw new XError("validate rus, isEmpty")
        }
        if (!UtWord.isAlphasKaz(kaz)) {
            throw new XError("validate kaz, isAlphasKaz: " + kaz)
        }
    }

    void validateRus(String[] rusArr) {
        for (String rus : rusArr) {
            try {
                validateRus(rus)
            } catch (Exception e) {
                //throw new XError("rus: " + rus + ", rusArr: " + rusArr + ", err: " + e.message)
                throw new XError(e.message)
            }
        }
    }


    long getFactType(String tagName) {
        StoreRecord recFactType = idxFactType.get(tagName)
        if (recFactType == null) {
            throw new XError("not found factType: " + tagName)
        }
        return recFactType.getLong("id")
    }

    long getTagType(String tagType_code) {
        StoreRecord recTag = this.idxTagType.get(tagType_code)
        //
        if (recTag == null) {
            notFoundThing.add(tagType_code)
            throw new XError("not found tagType: " + tagType_code)
        }
        //
        return recTag.getLong("id")
    }

    String[] getRus_arr(String dir, String rus) {
        rus = rus.trim().toLowerCase()

        if (dir.equals("1000-puzzle-english")) {
            String[] sArr = rus.split("/")
            for (int i = 0; i < sArr.length; i++) {
                String ss = sArr[i]
                sArr[i] = clearRus(ss)
            }
            return sArr

        } else if (dir.equals("1000-englishdom")) {
            String[] sArr = rus.split("/")
            for (int i = 0; i < sArr.length; i++) {
                String ss = sArr[i]
                sArr[i] = clearRus(ss)
            }
            return sArr

        } else {
            return [clearRus(rus)]
        }
    }

    String clearRus(String s) {
        s = s.trim().toLowerCase()

        s = s.replace(" ((!new!))", "")
        s = s.replace("((!new!))", "")
        s = s.replace(" (!new!)", "")
        s = s.replace("(!new!)", "")
        s = s.replace(" (tr!)", "")
        s = s.replace("(tr!)", "")
        s = s.replace("( ", "(")
        s = s.replace(" )", ")")
        s = s.replace("; ", ", ")
        s = s.replace("\u2013", "-") // &ndash;
        s = s.replace("\u2014", "-") // &dash;
        s = s.replace("\u00A0", " ") // &nbsp;
        s = s.replace("\u00A0", " ")
        s = s.replace("\u00A0", " ")
        s = s.replace("  ", " ")
        s = s.replace("  ", " ")
        s = s.replace("  ", " ")
        s = s.replace(" ...", "...")
        // рус "с" -> англ "с"
        s = s.replace("c", "с")
        s = s.replace("c", "с")
        // привет. -> привет
        if (s.endsWith(".") && !(s.contains("т.д.") || s.contains("т. д."))) {
            s = s.substring(0, s.length() - 1)
        }
        // привет; -> привет
        if (s.endsWith(";")) {
            s = s.substring(0, s.length() - 1)
        }
        // наивный /простак/ -> наивный (простак)
        //s="наивный /простак/"
        if (s.endsWith("/") && s.indexOf("/") != s.length()) {
            s = s.substring(0, s.length() - 1).replace("/", "(") + ")"
        }


        return s
    }

    String clearKaz(String s) {
        s = s.trim().toLowerCase()

        //
        s = s.replace("( ", "(")
        s = s.replace(" )", ")")
        s = s.replace("; ", ", ")
        s = s.replace("\u2013", "-") // &ndash;
        s = s.replace("\u2014", "-") // &dash;
        s = s.replace("\u00A0", " ") // &nbsp;
        s = s.replace("\u00A0", " ")
        s = s.replace("\u00A0", " ")
        s = s.replace("  ", " ")
        s = s.replace("  ", " ")
        s = s.replace("  ", " ")
        s = s.replace(" ...", "...")

        //
        if (s.endsWith(".") && !(s.endsWith("...") || s.contains("т.д.") || s.contains("т. д."))) {
            s = s.substring(0, s.length() - 1)
        }

        return s
    }


    String clearEng(String s) {
        s = s.trim().toLowerCase()

        s = s.replace(" ((!new!))", "")
        s = s.replace("((!new!))", "")
        s = s.replace(" (!new!)", "")
        s = s.replace("(!new!)", "")
        s = s.replace(" (tr!)", "")
        s = s.replace("(tr!)", "")

        return s
    }


    String clearTranscription(String s) {
        s = s.trim().toLowerCase()

        s = s.replace("]", "")
        s = s.replace("[", "")
        s = s.trim()

        return s
    }


    String getTopList(HashMap<String, Long> frMap, String dir, String eng) {
        long pos = getPos(frMap, dir, eng)
        if (pos <= 100) {
            return "top-100"
        } else if (pos <= 200) {
            return "top-200"
        } else if (pos <= 500) {
            return "top-500"
        } else if (pos <= 1000) {
            return "top-1000"
        } else if (pos <= 2000) {
            return "top-2000"
        } else if (pos <= 5000) {
            return "top-5000"
        } else {
            return ""
        }
    }

    long getPos(HashMap<String, Long> frMap, String dir, String eng) {
        long posMax = -1

        String[] ss = eng.split("[^\\w']+")
        for (String s : ss) {
            Long pos = frMap.get(s)
            if (pos != null && posMax < pos) {
                posMax = pos
            }
        }

        // Если явно сказано, что слово в списке "top-XXX"
        if (posMax == -1) {
            Integer minPos = dirsDefaultWordFrequency.get(dir)
            if (minPos != null) {
                posMax = minPos
            }
        }

        if (posMax == -1) {
            //println(eng)
            return Long.MAX_VALUE
        } else {
            return posMax
        }
    }

    List<String> getSoundFiles(String dirBase, Collection<String> dirsSound, String word) {
        List res = new ArrayList()

        if (dirsSound == null) {
            return res
        }

        for (String dirSound : dirsSound) {
            String soundDirName = dirBase + dirSound + "/"
            String soundFileName = soundDirName + word + ".mp3"
            soundFileName = soundFileName.replace("//", "/")

            String md5 = UtString.md5Str(word).toLowerCase().substring(0, 16)
            String soundFileNameMd5 = soundDirName + md5 + ".mp3"
            soundFileNameMd5 = soundFileNameMd5.replace("//", "/")

            // Если есть файл без проблем с кодировкой
            if (new File(soundFileName).exists()) {
                res.add(soundFileName)
            }

            // Если есть файл с преобразованным именем из-за проблем с кодировкой
            if (new File(soundFileNameMd5).exists()) {
                res.add(soundFileNameMd5)
            }
        }

        return res
    }

    String[] getWord_arr(String lang, String dir, String word) {
        switch (lang) {
            case "rus": {
                return getRus_arr(dir, word)
            }
            default: {
                throw new XError("clearWord, invalid lang: " + lang)
            }
        }
    }

    String getWord(String lang, String word) {
        switch (lang) {
            case "eng": {
                return clearEng(word)
            }
            case "kaz": {
                return clearKaz(word)
            }
            default: {
                throw new XError("clearWord, invalid lang: " + lang)
            }
        }
    }

    void validateWord(String lang, String word) {
        switch (lang) {
            case "eng": {
                validateEng(word)
                break
            }
            case "kaz": {
                validateKaz(word)
                break
            }
            default: {
                throw new XError("validateWord, invalid lang: " + lang)
            }
        }
    }

    void validateWord(String lang, String[] wordArr) {
        switch (lang) {
            case "rus": {
                validateRus(wordArr)
                break
            }
            default: {
                throw new XError("validateWord, invalid lang: " + lang)
            }
        }
    }

    public static Map<String, Integer> loadWordFrequencyMap(String fileName) {
        Map<String, Integer> wordFrequencyMap = new HashMap<>()

        //
        BufferedReader br = new BufferedReader(new FileReader(new File(fileName)))
        String line
        int pos = 0
        while ((line = br.readLine()) != null) {
            String[] ss = line.split(" ")
            String eng = ss[0]
            if (UtWord.isAlphasEng(eng)) {
                long frVal = Long.valueOf(ss[1])
                wordFrequencyMap.put(eng, pos)
                pos = pos + 1
            }
        }
        br.close()

        //
        return wordFrequencyMap
    }


    void addItemTag(long idItem, String tagTypeStr, String tagValue, stItemTag) {
        long tagType = getTagType(tagTypeStr)

        // С обеспечением уникальности значения тэга для idItem
        String key = idItem + "_" + tagType + "_" + tagValue
        if (!tagValueSet.contains(key)) {
            tagValueSet.add(key)
            long idItemTag = stItemTag.size() + this.genIdItemTag
            StoreRecord rec = stItemTag.add()
            rec.setValue("id", idItemTag)
            rec.setValue("item", idItem)
            rec.setValue("tagType", tagType)
            rec.setValue("tagValue", tagValue)
        }

    }

    void addFactTag(long idFact, String tagTypeStr, String tagValue, stFactTag) {
        long tagType = getTagType(tagTypeStr)

        long idFactTag = stFactTag.size() + this.genIdFactTag
        StoreRecord rec = stFactTag.add()
        rec.setValue("id", idFactTag)
        rec.setValue("fact", idFact)
        rec.setValue("tagType", tagType)
        rec.setValue("tagValue", tagValue)
    }


}