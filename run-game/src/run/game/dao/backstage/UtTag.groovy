package run.game.dao.backstage

import jandcode.commons.*
import run.game.dao.*

class UtTag {


    public static getFactLang(Map tag) {
        String valueLang

        long tagValue = UtCnv.toLong(tag.get(RgmDbConst.TagType_word_lang))
        valueLang = getFactLang(tagValue)
        if (valueLang != null) {
            return valueLang
        }

        tagValue = UtCnv.toLong(tag.get(RgmDbConst.TagType_word_translate_direction))
        valueLang = getFactLang(tagValue)
        if (valueLang != null) {
            return valueLang
        }

        return null
    }

    public static getFactLang(long tagValue) {
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
