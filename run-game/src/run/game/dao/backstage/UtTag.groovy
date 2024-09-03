package run.game.dao.backstage

import jandcode.commons.*
import run.game.dao.*

class UtTag {


    public static String getLangByTag(Map tag) {
        String valueLang

        String tagValue = UtCnv.toString(tag.get(RgmDbConst.TagType_word_lang))
        valueLang = getLangByTagValue(tagValue)
        if (valueLang != null) {
            return valueLang
        }

        tagValue = UtCnv.toString(tag.get(RgmDbConst.TagType_word_translate_direction))
        valueLang = getLangByTagValue(tagValue)
        if (valueLang != null) {
            return valueLang
        }

        return null
    }

    public static String getLangByTagValue(String tagValue) {
        switch (tagValue) {
            case RgmDbConst.Tag_word_lang_eng: return "eng"
            case RgmDbConst.Tag_word_lang_rus: return "rus"
            case RgmDbConst.Tag_word_lang_kaz: return "kaz"
            case RgmDbConst.Tag_word_translate_direction_eng_rus: return "rus"
            case RgmDbConst.Tag_word_translate_direction_rus_eng: return "eng"
            case RgmDbConst.Tag_word_translate_direction_kaz_rus: return "rus"
            case RgmDbConst.Tag_word_translate_direction_rus_kaz: return "kaz"
        }

        return null
    }

}
