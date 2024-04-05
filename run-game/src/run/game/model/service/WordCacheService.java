package run.game.model.service;

import jandcode.core.*;
import jandcode.core.store.*;

import java.util.*;

/**
 *
 */
public interface WordCacheService extends Comp {

    /**
     * Известные факты (индекс по значению)
     */
    Map<Object, List<StoreRecord>> getIdxFacts();

    Store getStFactSpelling();

    Store getStFactTranslate();


    /**
     * Стоп-слова при разборе сфотографировании текста
     */
    StoreIndex getIdxOcrStopWords();


}
