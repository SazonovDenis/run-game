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


/**
 * Хранилище разной информации, закешированной при старте.
 */
public class WordCacheServiceImpl extends BaseModelMember implements WordCacheService {

    //
    private Mdb mdb;

    //
    private StoreIndex idxFacts;
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
    public StoreIndex getIdxFacts() {
        return idxFacts;
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
        // Получаем spelling для всех слов из БД
        Fact_list list = mdb.create(Fact_list.class);
        stFactSpelling = list.loadFactsByDataType(RgmDbConst.DataType_word_spelling);
        stFactTranslate = list.loadFactsByDataType(RgmDbConst.DataType_word_translate);

        //
        Store stFact = mdb.createStore("Fact.list");
        stFactSpelling.copyTo(stFact);
        stFactTranslate.copyTo(stFact);
        idxFacts = stFact.getIndex("factValue");

        // Стоп-слова при разборе сфотографировании текста
        Store stOcrStopWords = mdb.createStore("OcrStopWords");
        RgmCsvUtils utils = mdb.create(RgmCsvUtils.class);
        utils.addFromCsv(stOcrStopWords, "res:run/game/dao/game/OcrStopWords.csv");
        idxOcrStopWords = stOcrStopWords.getIndex("value");
    }


}
