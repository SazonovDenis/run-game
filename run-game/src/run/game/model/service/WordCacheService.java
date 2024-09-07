package run.game.model.service;

import jandcode.core.*;
import jandcode.core.store.*;

import java.util.*;

/**
 *
 */
public interface WordCacheService extends Comp {

    /**
     * Известные факты (список)
     */
    Store getStFact();

    /**
     * Известные факты (индекс по значению)
     */
    Map<Object, List<StoreRecord>> getIdxFacts();

    /**
     * Стоп-слова при поиске в тексте (индекс по значению)
     */
    StoreIndex getIdxOcrStopWords();

}
