package run.game.dao.backstage

import jandcode.commons.*
import jandcode.core.store.*
import run.game.dao.*

class UtTag {


    public static String getLangByTag(Map tag) {
        String valueLang

        String tagValue = UtCnv.toString(tag.get(RgmDbConst.TagType_word_lang))
        valueLang = getLangByTagValue(tagValue)
        if (valueLang != null) {
            return valueLang
        }

        tagValue = UtCnv.toString(tag.get(RgmDbConst.TagType_translate_direction))
        valueLang = getLangByTagValue(tagValue)
        if (valueLang != null) {
            return valueLang
        }

        return null
    }


    public static String getLangByTagValue(String tagValue) {
        switch (tagValue) {
            case RgmDbConst.TagValue_word_lang_eng: return "eng"
            case RgmDbConst.TagValue_word_lang_rus: return "rus"
            case RgmDbConst.TagValue_word_lang_kaz: return "kaz"
            case RgmDbConst.TagValue_translate_direction_eng_rus: return "rus"
            case RgmDbConst.TagValue_translate_direction_rus_eng: return "eng"
            case RgmDbConst.TagValue_translate_direction_kaz_rus: return "rus"
            case RgmDbConst.TagValue_translate_direction_rus_kaz: return "kaz"
        }

        return null
    }


    /**
     * Сравниваем набры тэгов на совпадение
     * Наборы не равны, если есть тэг в обоих наборах, и его значение различается.
     */
    public static boolean tagsEquals(Map<Long, String> tags0, Map<Long, String> tags1) {

        if (tags1 == null || tags0 == null || tags1.size() == 0 || tags0.size() == 0) {
            return true
        }

        //
        boolean result = true
        //
        for (Map.Entry<Long, String> entry0 : tags0.entrySet()) {
            long tagKey = entry0.getKey()
            String tagValue0 = entry0.getValue()
            if (tags1.containsKey(tagKey)) {
                String tagValue1 = tags1.get(tagKey)
                if (!tagValue0.equals(tagValue1)) {
                    result = false
                    break
                }
            }
        }
        //
        for (Map.Entry<Long, String> entry1 : tags1.entrySet()) {
            long tagKey = entry1.getKey()
            String tagValue1 = entry1.getValue()
            if (tags0.containsKey(tagKey)) {
                String tagValue0 = tags0.get(tagKey)
                if (!tagValue1.equals(tagValue0)) {
                    result = false
                    break
                }
            }
        }

        //
        return result
    }

    public static void cleanStoreByTags(Store st, Map<Long, String> tags) {
        int n = st.size() - 1
        while (n >= 0) {
            StoreRecord rec = st.get(n)
            Map recTags = (Map) rec.getValue("tag")
            if (!tagsEquals(tags, recTags)) {
                st.remove(n)
            }
            n = n - 1
        }
    }


}
