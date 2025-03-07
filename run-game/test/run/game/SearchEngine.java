package run.game;

import java.util.*;

public class SearchEngine {

    public List<SearchIndexEl> data = new ArrayList<>();

    public int binarySearch(List<SearchIndexEl> lst, String word) {
        int res = -1;

        if (word == null || word.length() == 0) {
            return res;
        }

        //
        int posA = 0;
        int posB = lst.size() - 1;

        // Ищем начало
        while (posA <= posB) {
            int pos = (posA + posB) / 2;

            //
            SearchIndexEl el = lst.get(pos);
            String value = el.value;

            //
            boolean starts = value.startsWith(word);
            if (starts) {
                res = pos;
            }

            //
            int cmp = word.compareTo(value);
            if (cmp > 0) {
                posA = pos + 1;
            } else {
                posB = pos - 1;
            }
        }

        //
        return res;
    }


}
