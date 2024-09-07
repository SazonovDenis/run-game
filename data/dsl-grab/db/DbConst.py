class FactType:
    spelling = "1001"  # Написание слова
    translate = "1002"  # Перевод слова
    sound = "1003"  # Звучание слова
    picture = "1004"  # Изображение предмета
    transcription = "1005"  # Транскрибция слова
    idiom = "1006"  # Идиоматическое выражение
    example = "1007"  # Пример использования
    spelling_distorted = "1010"  # Альтернативное написание слова


class TagType:
    word_lang = "1001"  # Язык слова
    translate_direction = "1002"  # Направление перевода" слов
    word_part_of_speech = "1003"  # Часть речи
    word_use_sample = "1004"  # Пример использования" слов
    word_category = "1005"  # Тема слова
    level_grade = "1006"  # Уровень сложности
    top_list = "1007"  # Топ ххх слов
    word_sound_info = "1008"  # Звук для слова
    plan_access = "1010"  # Уровень доступа к плану
    plan_question_factType = "1011"  # Тип вопросов в плане
    plan_answer_factType = "1012"  # Тип ответов в плане
    dictionary = "1100"  # Пометка словаря (источника)


class TagValue:
    eng = "eng"
    rus = "rus"
    kaz = "kaz"
    eng_rus = "eng-rus"
    rus_eng = "rus-eng"
    rus_kaz = "rus-kaz"
    kaz_rus = "kaz-rus"
    eng_eng = "eng-eng"
    default = "default"
    dictionary_full = "full"
