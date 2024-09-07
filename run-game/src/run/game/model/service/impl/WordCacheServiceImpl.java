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
    private Map<Object, List<StoreRecord>> idxFacts;
    private Store stFact;
    private Store stFactSpelling;
    private Store stFactTranslate;
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
    public Map<Object, List<StoreRecord>> getIdxFacts() {
        return idxFacts;
    }

    @Override
    public Store getStFact() {
        return stFact;
    }

    @Override
    public Store getStFactSpelling() {
        return stFactSpelling;
    }

    @Override
    public Store getStFactTranslate() {
        return stFactTranslate;
    }

    @Override
    public StoreIndex getIdxOcrStopWords() {
        return idxOcrStopWords;
    }


    /**
     * Загружает список и готовит его к работе.
     */
    private void loadData() throws Exception {
        // Загружаем spelling-distorted для всех слов из БД.
        // Гарантируется, что в БД помимо единственного факта правильного написания word_spelling,
        // есть по крайней мере один факт word_spelling_distorted, предназначенного для поиска.
        // Если значение  word_spelling_distorted единственное, то его значение совпадает с word_spelling,
        // это означает, что вариантов написания у слова нет.
        Fact_list list = mdb.create(Fact_list.class);
        stFactSpelling = list.loadBy_factType(RgmDbConst.FactType_word_spelling_distorted, Arrays.asList(RgmDbConst.TagType_dictionary, RgmDbConst.TagType_word_lang));

        // Загружаем word-translate для всех слов из БД.
        stFactTranslate = list.loadBy_factType(RgmDbConst.FactType_word_translate, Arrays.asList(RgmDbConst.TagType_dictionary, RgmDbConst.TagType_translate_direction));

        //
        stFact = mdb.createStore("Fact.list");
        stFactSpelling.copyTo(stFact);
        stFactTranslate.copyTo(stFact);
        idxFacts = StoreUtils.collectGroupBy_records(stFact, "factValue");

        // Стоп-слова при разборе сфотографировании текста
        Store stOcrStopWords = mdb.createStore("OcrStopWords");
        RgmCsvUtils utils = mdb.create(RgmCsvUtils.class);
        utils.addFromCsv(stOcrStopWords, "res:run/game/dao/game/OcrStopWords.csv");
        idxOcrStopWords = stOcrStopWords.getIndex("value");
    }


}
