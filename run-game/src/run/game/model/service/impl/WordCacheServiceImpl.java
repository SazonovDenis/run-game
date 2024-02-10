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


/**
 * Менеджер/фабрика кубов
 */
public class WordCacheServiceImpl extends BaseModelMember implements WordCacheService {

    //
    private Mdb mdb;

    //
    private StoreIndex idxFacts;
    private Store stFactSpelling;
    private Store stFactTranslate;

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
    }


}
