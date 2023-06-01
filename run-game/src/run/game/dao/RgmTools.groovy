package run.game.dao

import jandcode.commons.*
import jandcode.core.dbm.mdb.*
import jandcode.core.store.*
import run.game.dao.backstage.*
import run.game.testdata.fixture.*

public class RgmTools implements IMdbLinkSet {

    Mdb mdb = null

    public void setMdb(Mdb mdb) {
        this.mdb = mdb
    }

    /**
     * Ищем слова из файла fileName среди наших словарных слов
     */
    public void loadItemsFromTextFile(String fileName, Collection<Long> items, Collection<String> wordsNotFound) {
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

    private String filterWord(String word) {
        // Так можно нащупать Item из нескольких слов (для словосочетаний, напимер: "go_on", "он_екі")
        word = word.replace("_", " ")
        //
        word = word.replace("(", "")
        word = word.replace(")", "")
        word = word.replace("!", "")
        if ((ItemFact_fb.isAlphasEng(word) || ItemFact_fb.isAlphasKaz(word)) && word.length() > 1) {
            return word.toLowerCase()
        }
        return null
    }


    private String tranformWord(String wordEng) {
        if (wordEng.endsWith("s")) {
            wordEng = wordEng.substring(0, wordEng.length() - 1)
            wordEng = filterWord(wordEng)
            return wordEng
        }
        return null
    }


}
