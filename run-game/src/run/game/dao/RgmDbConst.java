package run.game.dao;

public class RgmDbConst {

    public static long DataType_word_spelling = 1001;
    public static long DataType_word_translate = 1002;
    public static long DataType_word_sound = 1003;
    public static long DataType_word_picture = 1004;

    public static long Tag_word_lang_eng = 100;
    public static long Tag_word_lang_rus = 101;
    public static long Tag_word_lang_kaz = 102;

    public static long Tag_word_translate_direction_eng_rus = 200;
    public static long Tag_word_translate_direction_rus_eng = 201;
    public static long Tag_word_translate_direction_rus_kaz = 202;
    public static long Tag_word_translate_direction_kaz_rus = 203;
    public static long Tag_plan_access_default = 300;  // План по умолчанию

    public static long TagType_word_lang = 1001;  // Язык слова
    public static long TagType_word_translate_direction = 1002;  // Направление перевода слова
    public static long TagType_word_part_of_speech = 1003;  // Часть речи
    public static long TagType_word_use_sample = 1004;  // Пример использования слова
    public static long TagType_word_category = 1005;  // Тема слова
    public static long TagType_level_grade = 1006;  // Уровень сложности
    public static long TagType_top_list = 1007;  // Топ ххх слов
    public static long TagType_word_sound_info = 1008;  // Свойства звукового файла
    public static long TagType_plan_access = 3000;  // Уровень доступа к плану

}
