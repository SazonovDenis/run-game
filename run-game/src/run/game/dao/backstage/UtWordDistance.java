package run.game.dao.backstage;

import jandcode.core.store.*;
import org.apache.commons.lang3.*;

import java.util.*;

public class UtWordDistance {

    /**
     * Возвращает лучшие соответствия для слова word по расстонию Джаро-Винклера
     *
     * @param word слово
     * @param st   другие слова
     * @return лучшие соответсвия
     */
    public static Map<String, Double> getJaroWinklerMatch(String word, Store st, int maxMatchSize) {
        Map<String, Double> distances = new HashMap<>();

        //
        double distanceMinLevelToUse = 0.7;

        //
        for (int j = 0; j < st.size(); j++) {
            String wordPair = st.get(j).getString("value");
            // Самого с собой не сравниваем
            if (word.equals(wordPair)) {
                continue;
            }
            // Заметно различающиеся по длине
            if (word.length() / wordPair.length() > 2) {
                continue;
            }

            //
            double distancePair = StringUtils.getJaroWinklerDistance(word, wordPair);

            //
            if (distancePair > distanceMinLevelToUse) {
                // Вытесняем самую плохую пару
                if (distances.size() >= maxMatchSize) {
                    double distanceMin = 1E9;
                    String distanceMinWord = null;
                    for (String distanceWord : distances.keySet()) {
                        Double distance = distances.get(distanceWord);
                        if (distance < distanceMin) {
                            distanceMin = distance;
                            distanceMinWord = distanceWord;
                        }
                    }
                    if (distancePair > distanceMin) {
                        distances.remove(distanceMinWord);
                    }
                }
                // Добавляем новую пару, если место освободилось
                if (distances.size() < maxMatchSize) {
                    distances.put(wordPair, distancePair);
                }
            }
        }


        //
        return distances;
    }

}
