package run.game.testdata.fixture

import jandcode.commons.*
import jandcode.commons.error.*
import jandcode.core.dbm.fixture.*
import jandcode.core.store.*
import org.apache.commons.io.filefilter.*
import org.slf4j.*
import run.game.util.*

/**
 *
 */
class ItemFact_fb extends BaseFixtureBuilder {

    StoreIndex idxTag
    StoreIndex idxTagType
    StoreIndex idxDataType

    String dirBase = "data/web-grab/"

    def dirs = [
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

    def dirsDefaultWordFrequency = [
            "1000-englishdom"    : 1000,
            "1000-preply"        : 1000,
            "1000-puzzle-english": 1000,
            "1000-skyeng"        : 1000,
            "200-puzzle-english" : 200,
            //"5000-studynow"      : 5000,
    ]


    protected void onBuild() {
        FixtureTable fxItem = fx.table("Item")
        FixtureTable fxFact = fx.table("Fact")
        FixtureTable fxItemTag = fx.table("ItemTag")
        FixtureTable fxFactTag = fx.table("FactTag")

        //
        Logger log = UtLog.getLogConsole()
        LogerFiltered logCube = new LogerFiltered(log)

        //
        idxTag = mdb.loadQuery("select TagType.code || '_' || Tag.value as key, Tag.* from Tag join TagType on Tag.tagType = TagType.id").getIndex("key")
        idxTagType = mdb.loadQuery("select * from TagType").getIndex("code")
        idxDataType = mdb.loadQuery("select * from DataType").getIndex("code")

        //
        Store stCsvBad = mdb.createStore("dat.csv.bad")

        // Частота встречаемости
        Map<String, Integer> wordFrequencyMap = new HashMap<>()
        File fileFr = new File(dirBase + "en_50k.txt")
        BufferedReader br = new BufferedReader(new FileReader(fileFr))
        String s
        int pos = 0
        while ((s = br.readLine()) != null) {
            String[] ss = s.split(" ")
            String eng = ss[0]
            if (isAlphasEng(eng)) {
                long frVal = Long.valueOf(ss[1])
                wordFrequencyMap.put(eng, pos)
                pos = pos + 1
            }
        }
        br.close()


        //
        Map<String, StoreRecord> items = new HashMap<>()
        Map<String, Set<String>> itemCategories = new HashMap<>()
        long genIdItem = 0
        long genIdItemTag = 0
        long genIdFact = 0
        long genIdFactTag = 0
        long item = 0

        //
        for (String dir : dirs) {
            String[] files = new File(dirBase + dir).list(new WildcardFileFilter("dat*.csv"))
            for (String file : files) {
                String fileCsv = dirBase + dir + "/" + file

                //
                println()
                println("fileCsv: " + fileCsv)

                // Заполним из наших csv
                Store stCsv = mdb.createStore("dat.csv")
                addFromCsv(stCsv, fileCsv)

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
                        String eng = recCsv.getString("eng")
                        eng = clearEng(eng)
                        //
                        String rus = recCsv.getString("rus")
                        rus = clearRus(rus)

                        // Отсеиваем ошибки и мусор
                        validateEng(eng)
                        validateRus(rus)

                        // Первый раз встретили слово?
                        StoreRecord recItem = items.get(eng)
                        if (recItem == null) {
                            // Добавляем Item
                            recItem = stItem.add()
                            items.put(eng, recItem)
                            //
                            itemCategories.put(eng, new HashSet<String>())
                            //
                            genIdItem = genIdItem + 1
                            recItem.setValue("id", genIdItem)
                            recItem.setValue("value", eng)
                            //
                            item = recItem.getLong("id")

                            /*
                            // Добавляем ItemTag:level-grade
                            String levelGrade = getLevelGrade(eng)
                            StoreRecord recItemTag = stItemTag.add()
                            recItemTag.setValue("item", genItem)
                            recItemTag.setValue("tag", getTag("level-grade", levelGrade))
                            */

                            // Добавляем ItemTag:top-list
                            String topList = getTopList(wordFrequencyMap, dir, eng)
                            if (!UtCnv.isEmpty(topList)) {
                                genIdItemTag = genIdItemTag + 1
                                StoreRecord recItemTag_2 = stItemTag.add()
                                recItemTag_2.setValue("id", genIdItemTag)
                                recItemTag_2.setValue("item", item)
                                recItemTag_2.setValue("tag", getTag("top-list", topList))
                            }
                        } else {
                            item = recItem.getLong("id")
                        }

                        // Добавляем ItemTag:word-category
                        for (int n = 1; n <= 3; n++) {
                            String category = recCsv.getString("category" + n)
                            category = category.trim().toLowerCase()
                            if (!UtCnv.isEmpty(category)) {
                                // С обеспечением уникальности
                                if (!itemCategories.get(eng).contains(category)) {
                                    genIdItemTag = genIdItemTag + 1
                                    StoreRecord recItemTag = stItemTag.add()
                                    recItemTag.setValue("id", genIdItemTag)
                                    recItemTag.setValue("item", item)
                                    recItemTag.setValue("tag", getTag("word-category", category))
                                }
                            }
                        }

                        // Добавляем ItemTag:level-grade
                        String grade = recCsv.getString("grade")
                        grade = grade.trim().toLowerCase()
                        if (!UtCnv.isEmpty(grade)) {
                            genIdItemTag = genIdItemTag + 1
                            StoreRecord recItemTag = stItemTag.add()
                            recItemTag.setValue("id", genIdItemTag)
                            recItemTag.setValue("item", item)
                            recItemTag.setValue("tag", getTag("level-grade", grade))
                        }

                        // Добавляем Fact
                        genIdFact = genIdFact + 1
                        StoreRecord recFact = stFact.add()
                        recFact.setValue("id", genIdFact)
                        recFact.setValue("item", item)
                        recFact.setValue("dataType", getDataType("word-spelling"))
                        recFact.setValue("value", eng)
                        //
                        String transcribtion = recCsv.getString("trans")
                        if (!UtCnv.isEmpty(transcribtion)) {
                            genIdFact = genIdFact + 1
                            StoreRecord recFact_3 = stFact.add()
                            recFact_3.setValue("id", genIdFact)
                            recFact_3.setValue("item", item)
                            recFact_3.setValue("dataType", getDataType("word-transcribtion"))
                            recFact_3.setValue("value", transcribtion)
                        }
                        //
                        genIdFact = genIdFact + 1
                        StoreRecord recFact_1 = stFact.add()
                        recFact_1.setValue("id", genIdFact)
                        recFact_1.setValue("item", item)
                        recFact_1.setValue("dataType", getDataType("word-translate"))
                        recFact_1.setValue("value", rus)
                        // Добавляем FactTag
                        genIdFactTag = genIdFactTag + 1
                        StoreRecord recFactTag = stFactTag.add()
                        recFactTag.setValue("id", genIdFactTag)
                        recFactTag.setValue("fact", recFact_1.getLong("id"))
                        recFactTag.setValue("tag", getTag("word-translate-direction", "en-ru"))

                        //
                        logCube.logStepStep()
                    }
                    catch (Exception e) {
                        StoreRecord recCsvBad = stCsvBad.add(recCsv.getValues())
                        recCsvBad.setValue("error", e.getMessage())
                        recCsvBad.setValue("file", fileCsv)
                    }
                }
            }
        }

        //
        println()
        println("All dirs done")


        //
        saveToCsv(stCsvBad, new File(dirBase + "../../temp/bad.csv"))
    }


    void addFromCsv(Store store, String fileName) {
        StoreService svcStore = getModel().getApp().bean(StoreService.class)
        StoreLoader ldr = svcStore.createStoreLoader("csv.game")
        ldr.setStore(store)
        ldr.load().fromFileObject(fileName)
    }


    void saveToCsv(Store tab, File file) throws Exception {
        String delim = "\t"

        StringBuilder res = new StringBuilder()

        for (StoreField f : tab.getFields()) {
            if (f.getIndex() != 0) {
                res.append(delim)
            }
            res.append(f.getName())
        }
        res.append("\r\n")

        for (StoreRecord rec : tab) {
            for (StoreField f : tab.getFields()) {
                if (f.getIndex() != 0) {
                    res.append(delim)
                }
                if (rec.isValueNull(f.getName())) {
                } else if (rec.getValue(f.getName()) instanceof String) {
                    res.append("\"")
                    res.append(rec.getString(f.getName()).replace("\"", "\\\""))
                    res.append("\"")
                } else {
                    res.append(rec.getString(f.getName()))
                }
            }
            res.append("\r\n")

        }

        //
        UtFile.saveString(res.toString(), file)
    }


    public static boolean isAlphasEng(String s) {
        return s != null && s.matches("^[a-zA-Z -]*\$")
    }

    public static boolean isAlphasRus(String s) {
        return s != null && s.matches("^[а-яА-Я -]*\$")
    }


    boolean validateEng(String eng) {
        if (isAlphasEng(eng)) {
            return true
        }
        throw new XError("validate eng: " + eng)
    }


    boolean validateRus(String rus) {
        if (isAlphasRus(rus)) {
            return true
        }
        throw new XError("validate rus: " + rus)
    }


    long getDataType(String tagName) {
        StoreRecord recDataType = idxDataType.get(tagName)
        if (recDataType == null) {
            throw new XError("not found dataType: " + tagName)
        }
        return recDataType.getLong("id")
    }


    String getLevelGrade(String eng) {
        return "A1 (Beginner)"
    }


    long getTag(String tagType, String value) {
        StoreRecord recTag = this.idxTag.get(tagType + "_" + value)
        if (recTag == null) {
            throw new XError("not found tagType: " + tagType + ", value: " + value)
        }
        return recTag.getLong("id")
    }


    String clearRus(String s) {
        s = s.trim().toLowerCase()

        s = s.replace(" ((!new!))", "")
        s = s.replace("((!new!))", "")
        s = s.replace(" (!new!)", "")
        s = s.replace("(!new!)", "")
        s = s.replace(" (tr!)", "")
        s = s.replace("(tr!)", "")

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
}