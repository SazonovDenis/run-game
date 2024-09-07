package run.game.model.service.impl;

import jandcode.core.*;
import jandcode.core.db.*;
import jandcode.core.dbm.*;
import jandcode.core.dbm.impl.*;
import jandcode.core.dbm.mdb.*;
import jandcode.core.store.*;
import run.game.dao.*;
import run.game.dao.backstage.*;
import run.game.model.service.*;
import run.game.util.*;

import java.util.*;


/**
 * Хранилище разной информации, закешированной при старте.
 */
public class WordCacheServiceImpl extends BaseModelMember implements WordCacheService {

    //
    private Mdb mdb;

    //
    private Store stFact;
    private Map<Object, List<StoreRecord>> idxFacts;
    private StoreIndex idxOcrStopWords;


    @Override
    protected void onConfigure(BeanConfig cfg) throws Exception {
        super.onConfigure(cfg);

        //
        Db db = getModel().createDb();
        Db dbw = new ModelDbWrapper(db, true, true);
        mdb = getModel().createMdb(dbw);

        //
        loadData();
    }

    @Override
    public Store getStFact() {
        return stFact;
    }

    @Override
    public Map<Object, List<StoreRecord>> getIdxFacts() {
        return idxFacts;
    }

    @Override
    public StoreIndex getIdxOcrStopWords() {
        return idxOcrStopWords;
    }


    /**
     * Загружает список всех слов из БД и готовит его к работе.
     */
    private void loadData() throws Exception {
        // --- idxFacts
        Fact_list list = mdb.create(Fact_list.class);

        // Загружаем spelling-distorted для всех слов из БД.
        Store stFactSpelling_distorted = list.loadBy_factType(RgmDbConst.FactType_word_spelling_distorted, Arrays.asList(RgmDbConst.TagType_dictionary, RgmDbConst.TagType_word_lang));
        // Загружаем spelling для всех слов из БД.
        Store stFactSpelling = list.loadBy_factType(RgmDbConst.FactType_word_spelling, Arrays.asList(RgmDbConst.TagType_dictionary, RgmDbConst.TagType_word_lang));
        // Загружаем word-translate для всех слов из БД.
        Store stFactTranslate = list.loadBy_factType(RgmDbConst.FactType_word_translate, Arrays.asList(RgmDbConst.TagType_dictionary, RgmDbConst.TagType_translate_direction));

        //
        stFact = mdb.createStore("Fact.list");
        //
        stFactSpelling.copyTo(stFact);
        stFactSpelling_distorted.copyTo(stFact);
        stFactTranslate.copyTo(stFact);
        //
        idxFacts = StoreUtils.collectGroupBy_records(stFact, "factValue");

        // ---idxOcrStopWords
        // Стоп-слова при разборе сфотографировании текста
        Store stOcrStopWords = mdb.createStore("OcrStopWords");
        RgmCsvUtils utils = mdb.create(RgmCsvUtils.class);
        utils.addFromCsv(stOcrStopWords, "res:run/game/dao/game/OcrStopWords.csv");
        idxOcrStopWords = stOcrStopWords.getIndex("value");
    }


}
