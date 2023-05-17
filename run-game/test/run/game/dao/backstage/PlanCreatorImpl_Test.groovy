package run.game.dao.backstage

import jandcode.commons.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*
import run.game.testdata.fixture.*

class PlanCreatorImpl_Test extends RgmBase_Test {


    @Test
    void createTasks() {
        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)

        // Ищем слова из файла среди словарных слов
        Collection<Long> items = new ArrayList<>()
        Collection<String> wordsNotFound = new ArrayList<>()
        loadItemsFromTextFile("test/run/game/dao/backstage/PlanCreatorImpl_3.txt", items, wordsNotFound)

        //
        println("found: " + items.size())
        println(items)
        println("notFound: " + wordsNotFound.size())
        println(wordsNotFound)

        //
        //DataBox plan = planCreator.createPlan("Robinson", items, RgmDbConst.DataType_word_spelling, RgmDbConst.DataType_word_translate, 3)
        Collection<DataBox> tasks = planCreator.createTasks(items, RgmDbConst.DataType_word_spelling, RgmDbConst.DataType_word_translate, 3)

        //
        println()
        for (DataBox task : tasks) {
            printTaskOneLine(task)
        }
    }


    @Test
    void createPlan() {
        PlanCreatorImpl planCreator = mdb.create(PlanCreatorImpl)

        // Ищем слова из файла среди словарных слов
        Collection<Long> items = new ArrayList<>()
        Collection<String> wordsNotFound = new ArrayList<>()
        loadItemsFromTextFile("test/run/game/dao/backstage/PlanCreatorImpl_3.txt", items, wordsNotFound)

        //
        println("found: " + items.size())
        println(items)
        println("notFound: " + wordsNotFound.size())
        println(wordsNotFound)

        //
        long idPlan = planCreator.createPlan("Robinson", items, RgmDbConst.DataType_word_spelling, RgmDbConst.DataType_word_translate, 3)

        //
        println("idPlan: " + idPlan)
    }


    /**
     * Ищем слова из файла fileName среди наших словарных слов
     */
    void loadItemsFromTextFile(String fileName, Collection<Long> items, Collection<String> wordsNotFound) {
        // Грузим все слова из БД
        Fact_list list = mdb.create(Fact_list)
        Store stFacts = list.loadFactsByDataType(RgmDbConst.DataType_word_spelling)
        StoreIndex idxFacts = stFacts.getIndex("factValue")

        // Грузим слова из файла
        String strFile = UtFile.loadString(fileName)
        String[] arrWords = strFile.split()

        // Повторы не нужны
        Set<String> setWords = new HashSet<>()
        for (String word : arrWords) {
            word = filterWord(word)
            if (word != null) {
                setWords.add(word)
            }
        }

        //
        for (String word : setWords) {
            StoreRecord rec = idxFacts.get(word)
            if (rec != null) {
                items.add(rec.getLong("item"))
            } else {
                String word1 = tranformWord(word)
                if (word1 != null) {
                    rec = idxFacts.get(word1)
                    if (rec != null) {
                        items.add(rec.getLong("item"))
                    } else {
                        wordsNotFound.add(word)
                    }
                } else {
                    wordsNotFound.add(word)
                }
            }
        }
    }

    String filterWord(String wordEng) {
        wordEng = wordEng.replace("(", "")
        wordEng = wordEng.replace(")", "")
        wordEng = wordEng.replace("!", "")
        if (ItemFact_fb.isAlphasEng(wordEng) && wordEng.length() > 1) {
            return wordEng.toLowerCase()
        }
        return null
    }

    String tranformWord(String wordEng) {
        if (wordEng.endsWith("s")) {
            wordEng = wordEng.substring(0, wordEng.length() - 1)
            wordEng = filterWord(wordEng)
            return wordEng
        }
        return null
    }


}
