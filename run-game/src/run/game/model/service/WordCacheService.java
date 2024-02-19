package run.game.model.service;

import jandcode.core.*;
import jandcode.core.store.*;

/**
 *
 */
public interface WordCacheService extends Comp {


    /**
     * Известные факты (индекс по значению)
     */
    StoreIndex getIdxFacts();

    Store getStFactSpelling();

    Store getStFactTranslate();


    /**
     * Стоп-слова при разборе сфотографировании текста
     */
    StoreIndex getIdxOcrStopWords();


}
