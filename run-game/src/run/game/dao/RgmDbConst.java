package run.game.dao;

public class RgmDbConst {

    public static long FactType_word_spelling = 1001;
    public static long FactType_word_translate = 1002;
    public static long FactType_word_sound = 1003;
    public static long FactType_word_picture = 1004;
    public static long FactType_word_transcription = 1005;
    public static long FactType_word_idiom = 1006;
    public static long FactType_word_example = 1007;
    public static long FactType_word_spelling_distorted = 1010;

    public static String TagValue_word_lang_eng = "eng"; //100;
    public static String TagValue_word_lang_rus = "rus"; //101;
    public static String TagValue_word_lang_kaz = "kaz"; //102;

    public static String TagValue_translate_direction_eng_rus = "eng-rus"; // 200;
    public static String TagValue_translate_direction_rus_eng = "rus-eng"; // 201;
    public static String TagValue_translate_direction_rus_kaz = "rus-kaz"; // 202;
    public static String TagValue_translate_direction_kaz_rus = "kaz-rus"; // 203;

    public static String TagValue_plan_access_default = "default";         // План по умолчанию

    public static String TagValue_dictionary_base = "base";                // Малый словарь
    public static String TagValue_dictionary_full = "full";                // Полный словарь

    public static long TagType_word_lang = 1001;                // Язык слова
    public static long TagType_translate_direction = 1002;      // Направление перевода слова
    public static long TagType_word_part_of_speech = 1003;      // Часть речи
    public static long TagType_word_use_sample = 1004;          // Пример использования слова
    public static long TagType_word_category = 1005;            // Тема слова
    public static long TagType_level_grade = 1006;              // Уровень сложности
    public static long TagType_top_list = 1007;                 // Топ ххх слов
    public static long TagType_word_sound_info = 1008;          // Звук для слова
    public static long TagType_plan_access = 1010;              // Уровень доступа к плану
    public static long TagType_dictionary = 1100;               // Пометка словаря (источника)

    public static long LinkType_myFriend = 1001;   // Друг
    public static long LinkType_myParent = 1002;   // Родитель
    public static long LinkType_myChild = 1003;    // Ребенок
    public static long LinkType_myTeacher = 1004;  // Учитель
    public static long LinkType_myStudent = 1005;  // Ученик
    public static long LinkType_blockedByMe = 2000;  // Заблокирован

    public static long ConfirmState_waiting = 1001;   // Ожидает
    public static long ConfirmState_accepted = 1002;  // Принят
    public static long ConfirmState_refused = 1003;   // Отвергнут
    public static long ConfirmState_cancelled = 1004; // Отменен


}
