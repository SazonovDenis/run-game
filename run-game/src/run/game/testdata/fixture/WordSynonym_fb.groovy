package run.game.testdata.fixture

import jandcode.commons.*
import jandcode.core.dbm.fixture.*
import jandcode.core.store.*
import org.slf4j.*
import run.game.util.*

/**
 *
 */
class WordSynonym_fb extends BaseFixtureBuilder {

    String dirBase = "data/web-grab/"

    Logger log = UtLog.getLogConsole()

    protected void onBuild() {
        FixtureTable fxWordSynonym = fx.table("WordSynonym")

        //
        LogerFiltered logger = new LogerFiltered(log)
        logger.logStepStart()

        //
        Store stDataType = mdb.loadQuery("select * from DataType")
        StoreIndex idxDataType = stDataType.getIndex("code")
        Store st_rus = mdb.loadQuery("select * from Fact where dataType = " + idxDataType.get("word-translate").getLong("id"))
        StoreIndex idx_rus = st_rus.getIndex("value")


        // Заполним из csv
        Map<String, Set<String>> mapSynonyms = new HashMap<>()

        //
        File fileFr = new File(dirBase + "rus_synonyms.txt")
        BufferedReader br = new BufferedReader(new FileReader(fileFr))
        String s
        while ((s = br.readLine()) != null) {
            String[] ss = s.split("[|]")
            if (ss.length == 0) {
                continue
            }
            String word = ss[0]

            //
            if (idx_rus.get(word) != null) {
                Set set = mapSynonyms.get(word)
                if (set == null) {

                    set = new ArrayList()
                    mapSynonyms.put(word, set)
                }

                //
                for (int i = 1; i < ss.length; i++) {
                    set.add(ss[i])
                }

            }

            //
            logger.logStepStep()
        }
        //
        br.close()


        //
        Store stWordSynonym = fxWordSynonym.getStore()
        int row = 1
        for (String word : mapSynonyms.keySet()) {
            Set set = mapSynonyms.get(word)
            stWordSynonym.add([id: row, lang: "rus", word: word, synonyms: UtJson.toJson(set)])
            row = row + 1
        }

    }

    void addFromCsv(Store store, String fileName) {
        StoreService svcStore = getModel().getApp().bean(StoreService.class)
        StoreLoader ldr = svcStore.createStoreLoader("csv")
        ldr.setStore(store)
        ldr.load().fromFileObject(fileName)
    }

}