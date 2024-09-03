class FactType:
    spelling = "1001"  # Написание слова
    translate = "1002"  # Перевод слова
    sound = "1003"  # Звучание слова
    picture = "1004"  # Изображение предмета
    transcription = "1005"  # Транскрибция слова
    idiom = "1006"  # Идиоматическое выражение
    example = "1007"  # Пример использования


class TagType:
    word_lang = "1001"  # Язык слова
    word_translate_direction = "1002"  # Направление перевода" слов
    word_part_of_speech = "1003"  # Часть речи
    word_use_sample = "1004"  # Пример использования" слов
    word_category = "1005"  # Тема слова
    level_grade = "1006"  # Уровень сложности
    top_list = "1007"  # Топ ххх слов
    word_sound_info = "1008"  # Звук для слова
    plan_access = "1010"  # Уровень доступа к плану
    plan_question_factType = "1011"  # Тип вопросов в плане
    plan_answer_factType = "1012"  # Тип ответов в плане


class Tag:
    eng = "100"
    rus = "101"
    kaz = "102"
    eng_rus = "200"
    rus_eng = "201"
    rus_kaz = "202"
    kaz_rus = "203"
    eng_eng = "204"
    default = "300"

class TagValue:
    eng = "eng"
    rus = "rus"
    kaz = "kaz"
    eng_rus = "eng_rus"
    rus_eng = "rus_eng"
    rus_kaz = "rus_kaz"
    kaz_rus = "kaz_rus"
    eng_eng = "eng_eng"
    default = "default"
