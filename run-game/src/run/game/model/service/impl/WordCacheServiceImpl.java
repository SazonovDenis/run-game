package run.game.model.service.impl;

import jandcode.core.*;
import jandcode.core.db.*;
import jandcode.core.dbm.*;
import jandcode.core.dbm.impl.*;
import jandcode.core.dbm.mdb.*;
import jandcode.core.store.*;
import org.slf4j.*;
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

    //
    protected static Logger log = LoggerFactory.getLogger(ItemFinder.class);

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
        log.info("loadData");

        // --- idxFacts
        Fact_list list = mdb.create(Fact_list.class);

        // Загружаем word_spelling и word_spelling_distorted для всех слов из БД
        stFact = mdb.createStore("Fact.list");
        list.loadBy_factType(
                stFact,
                Arrays.asList(RgmDbConst.FactType_word_spelling, RgmDbConst.FactType_word_spelling_distorted),
                Arrays.asList(RgmDbConst.TagType_dictionary, RgmDbConst.TagType_word_lang)
        );

        //
        idxFacts = StoreUtils.collectGroupBy_records(stFact, "factValue");


        // --- Чтобы работал бинарный поиск по началу строки
        stFact.sort("factValue");


        // ---idxOcrStopWords
        // Стоп-слова при разборе сфотографировании текста
        Store stOcrStopWords = mdb.createStore("OcrStopWords");
        RgmCsvUtils utils = mdb.create(RgmCsvUtils.class);
        utils.addFromCsv(stOcrStopWords, "res:run/game/dao/game/OcrStopWords.csv");
        idxOcrStopWords = stOcrStopWords.getIndex("value");

        //
        log.info("loadData - ok");
    }


}
