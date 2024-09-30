from enum import IntEnum

import TextUtils as textUtils
from Parser import ParserBase


class ParserDict(ParserBase):
    class State(IntEnum):
        WAIT_WORD = 1
        WAIT_INFO = 2
        WAIT_TRANSLATE = 3
        COLLECT_EXAMPLE = 4
        COLLECT_TRANSLATE = 5
        COLLECT_IDIOMS = 6

    currentTranslation = None
    currentExample = None

    currentString = ""
    currentStringRaw = ""
    currentStringTags = []

    modePometa = False

    # Из помет в английском словаре мы берем тэги, напр:
    # [p]муз.[/p]
    # дает тэг "муз."
    currentPometas = []

    groupTags = None
    groupLevel = None
    groupLevelNumber = None

    WAIT_TAG_M2_AS_TRASLATION = False
    WAIT_TAG_DIMGRAY_AS_EXAMPLE_SIGN = False
    KZ_DICT_EXAMPLE_MODE = False
    KZ_DICT_POMETA_MODE = False
    KZ_DICT_SEMICOLUMN_SEPARATOR_MODE = False

    def __init__(self):
        self.state = self.State.WAIT_WORD
        self.token = None
        self.tokenType = None

    # Удаляет все пометы в строке
    def deletePometas(self, text):
        while len(text) > 0:
            n0 = text.find("{")
            n1 = text.find("}")
            if n0 == -1 or n1 == -1:
                break
            text = text[0:n0] + text[n1 + 1:]

        text = text.strip()

        return text

    # Удаляет пометы, находящиеся в начале строки
    def deleteStartingPometas(self, text):
        while len(text) > 0:
            text = text.strip()

            n0 = text.find("{")
            n1 = text.find("}")
            if n0 != 0 or n1 == -1:
                break
            text = text[0:n0] + text[n1 + 1:]

        #
        text = text.strip()

        return text

    def isLike_KzDict_Example(self, text):
        token_text = self.token["text"]
        if text.lower().strip().find(token_text) != -1:
            return True
        else:
            return False

    def isLike_KzDict_Translation(self, text):
        token_text = self.token["text"]
        if text.lower().strip().find(token_text) != -1:
            return False
        elif text.strip()[0:1] == "-":
            return False
        else:
            return True

    def isValid_translation(self, translation):
        translation_text = translation["text"]

        #
        if (translation["examples"] != None and len(translation["examples"]) != 0):
            return True

        #
        cleanText = textUtils.cleanNumberLevel(translation_text)
        cleanText = self.deletePometas(cleanText)
        if len(cleanText) == 0:
            return False

        #
        return True

    def cleanupData(self, token):
        # if self.token["text"] == "қасқыр":
        #    pass

        translations = token["translations"]
        translationsLen = len(translations)
        for translation_idx in range(translationsLen - 1, -1, -1):
            translation = translations[translation_idx]
            examples = translation["examples"]

            #
            if (not self.isValid_translation(translation)):
                translations.pop(translation_idx)
                continue

            # Уберем любые цифры спереди
            cleanText = textUtils.cleanNumberLevel(translation["text"])
            # Уберем пометы спереди
            cleanText = self.deleteStartingPometas(cleanText)
            #
            translation["text"] = cleanText

            #
            for example in examples:
                example_text = example["text"].strip()
                example["text"] = example_text

    def postpocessData(self, token):
        # ---
        # Распространим тэги с верхнего уровня вниз
        translations = token["translations"]
        for translation_idx in range(0, len(translations)):
            translation = translations[translation_idx]
            translation_text = translation["text"]
            translation_tags = translation["tags"]

            # Распространим тэги с верхнего уровня
            cleanText = textUtils.cleanNumberLevel(translation_text)
            cleanText = self.deletePometas(cleanText)
            #
            (groupLevel, groupLevelNumber) = textUtils.getTextLevelNumber(translation_text)
            if len(cleanText) == 0:
                # Запомним тэги уровня
                self.groupLevel = groupLevel
                self.groupLevelNumber = groupLevelNumber
                self.groupTags = translation_tags
            else:
                if self.groupTags != None:
                    if groupLevel > self.groupLevel:
                        # Унаследуем тэги уровня
                        translation_tags.extend(self.groupTags)
                    else:
                        # Сбросим тэги уровня
                        self.groupTags = None
                else:
                    # Сбросим тэги уровня
                    self.groupTags = None

        # ---
        # Добавляем Fact:word-spelling-distorted - искаженное написание
        # Актуально для казахского языка: замена букв с хвостиками на обычные.
        # Облегчает поиск при отсутствии/незнании казахской раскладки.
        spelling = token["text"]
        spelling_distorted = self.makeDistorted(spelling)
        if spelling_distorted != spelling:
            token["spelling_distorted"] = spelling_distorted

    # Актуально для казахского языка: заменяет буквы с хвостиками на обычные.
    # Облегчает поиск при отсутствии/незнании казахской раскладки.
    def makeDistorted(self, word):
        s1 = "әғқңөұүhіүқ ё"
        s2 = "эгкноуухиук е"

        word_Distorted = word
        for i in range(0, len(s1)):
            word_Distorted = word_Distorted.replace(s1[i:i + 1], s2[i:i + 1])

        return word_Distorted

    def fillPometasTemp(self, token):
        translations = token["translations"]
        for translation_idx in range(0, len(translations)):
            translation = translations[translation_idx]
            translation_text = translation["text"]
            translation_tags = translation["tags"]

            for tag in translation_tags:
                tagStr = "{" + tag + "}"
                if translation_text.find(tagStr) == -1:
                    translation_text = tagStr + " " + translation_text
                else:
                    pass

            translation["text"] = translation_text

    def clearToken(self):
        self.token = None
        self.tokenType = None
        self.currentTranslations = None

    def flushToken(self):
        if self.token != None:
            self.postpocessData(self.token)
            self.cleanupData(self.token)

            # ВРЕМЕННО!
            # Заполним тэгами текст перевода
            # Пока не сделали нормальную загрузки и отображение произвольных (а не только языковых) тэгов
            self.fillPometasTemp(self.token)

            #
            self.onToken(self.token, self.tokenType)
            #
            self.clearToken()

    def createToken(self, text):
        self.token = {}
        self.tokenType = "item"
        self.token["text"] = textUtils.cleanNumberLevel(text)
        self.token["idioms"] = []
        self.token["translations"] = []
        self.currentTranslation = None

    def setTranscription(self, text):
        self.token["transcription"] = textUtils.cleanNumberLevel(text)

    def addTranslation(self, text):
        translation = {}
        translation["text"] = text  # self.cleanupText(text)
        translation["tags"] = self.currentPometas
        translation["examples"] = []
        self.token["translations"].append(translation)
        #
        self.currentTranslation = translation

    def addExample(self, text):
        # Для некоторых слов нет перевода, например суффиксов, производных слов и т.п.
        # Например суффикс "`d" в английском словаре или слово "абордажный" в русском.
        # А примеры навешивать на что-то надо.
        if (self.currentTranslation == None):
            self.addTranslation("")

        example = {}
        example["text"] = textUtils.cleanNumberLevel(text)
        example["tags"] = self.currentPometas
        self.currentTranslation["examples"].append(example)

    def addIdiom(self, text):
        idiom = {}
        idiom["text"] = textUtils.cleanNumberLevel(text)
        self.token["idioms"].append(idiom)

    def currentStringFlushInternal(self, currentString):
        if self.state == self.State.WAIT_WORD:
            self.createToken(currentString)

        elif self.state == self.State.WAIT_INFO:
            self.setTranscription(currentString)

        if self.KZ_DICT_EXAMPLE_MODE:
            if self.isLike_KzDict_Example(currentString):
                self.addExample(currentString)
            elif self.isLike_KzDict_Translation(currentString):
                self.addTranslation(currentString)
            else:
                # строка ничинается с минуса "-" - это добавление текста
                # к последнему переводу или примеру,
                # без добавления нового перевода или примера
                currentString = currentString.strip(" -")
                if len(currentString) != 0:
                    translations = self.token["translations"]
                    if len(translations) != 0:
                        last_translation = translations[len(translations) - 1]
                        last_translation_examples = last_translation["examples"]
                        if len(last_translation_examples) == 0:
                            last_translation["text"] = last_translation["text"] + currentString
                        else:
                            last_translation_example = last_translation_examples[len(last_translation_examples) - 1]
                            last_translation_example["text"] = last_translation_example["text"] + currentString
                    else:
                        self.onError(self.token, "unmached currentString: " + currentString)
                        # self.addTranslation(currentString)

        elif self.state == self.State.COLLECT_TRANSLATE:
            self.addTranslation(currentString)

        elif self.state == self.State.COLLECT_EXAMPLE:
            self.addExample(currentString)

        elif self.state == self.State.COLLECT_IDIOMS:
            self.addIdiom(currentString)

    def currentStringFlush(self):
        currentString = self.currentString.replace("\xa0", " ")
        currentString = currentString.replace("\t", " ")
        currentString = currentString.strip()
        if currentString == "":
            return

        #
        if self.KZ_DICT_SEMICOLUMN_SEPARATOR_MODE:
            currentStrings = currentString.strip("@").split("@")
            if len(currentStrings) > 1:
                for currentString in currentStrings:
                    self.currentStringFlushInternal(currentString)
            else:
                self.currentStringFlushInternal(currentString)
        else:
            self.currentStringFlushInternal(currentString)

        #
        self.currentString = ""
        self.currentStringRaw = ""
        self.currentPometas = []
        self.tagsReset()

    def currentStringCollect(self, text):
        self.currentStringRaw = self.currentStringRaw + text

        self.collectPometa(text)

        if self.modePometa == True:
            self.currentString = self.currentString + "{" + text + "}"
        else:
            self.currentString = self.currentString + text

    def startPometa(self):
        self.modePometa = True
        self.currentPometa = ""

    def collectPometa(self, text):
        if self.modePometa == True:
            self.currentPometa = self.currentPometa + text

    def stopPometa(self):
        if self.currentPometa != None and len(self.currentPometa) != 0:
            self.currentPometas.append(self.currentPometa)
        #
        self.modePometa = False
        self.currentPometa = None

    def tagsPush(self, text):
        # [c red] -> c
        if text.find(" ") != -1:
            text = text.split(" ")[0]
        #
        self.currentStringTags.append(text)
        pass

    def tagsPop(self, text):
        posCurr = len(self.currentStringTags) - 1

        # Стек не пуст
        if posCurr == -1:
            raise Exception("len(self.currentStringTags) == 0")

        # [c red] -> c
        if text.find(" ") != -1:
            text = text.split(" ")[0]

        # Тэг m ищем на любую глубину и извлекаем из стека всё, что выше
        if text == "m":
            # Ищем вхождение
            popToPop = posCurr
            while popToPop >= 0 and self.currentStringTags[popToPop][0:1] != "m":
                popToPop = popToPop - 1
            # Нашли?
            if popToPop == -1:
                raise Exception("self.currentStringTags[posCurr] != " + text + ", currentStringTags[posCurr]: " + self.currentStringTags[posCurr])
            # Извлекаем из стека остальное
            while posCurr >= popToPop:
                self.currentStringTags.pop()
                posCurr = posCurr - 1
            #
            return

        # Совпадение извлекаемого имени
        if self.currentStringTags[posCurr] != text:
            return

        # Извлекаем
        self.currentStringTags.pop()

    def tagsReset(self):
        self.currentStringTags = []
        pass

    def handle(self, text, tokenType):

        if tokenType == "new-line":
            self.flushToken()
            #
            self.state = self.State.WAIT_WORD
            #
            return

        if self.KZ_DICT_SEMICOLUMN_SEPARATOR_MODE:
            # Есть точка с запятой в середине строки?
            if text.find(";") != -1:
                # Точка с запятой считается разделителем, только внутри тэга тэга m1, m2 и т.д.
                if len(self.currentStringTags) == 1 and self.currentStringTags[0] == "m2":
                    # Добавляем сильный разделитель (для последующей обработки)
                    text = text.replace(";", "@")
                pass

        if tokenType == "tag":

            self.tagsPush(text)

            if text == "p":
                self.startPometa()

                #
                return

            if self.KZ_DICT_POMETA_MODE and text == "c darkorange":
                self.startPometa()

                #
                return

            if text == "trn":
                self.currentStringFlush()
                #
                self.state = self.State.WAIT_TRANSLATE
                #
                return

            if self.WAIT_TAG_M2_AS_TRASLATION and text == "m2":
                if self.token["text"] == "сыр":
                    pass
                if self.state == self.State.WAIT_INFO:
                    self.currentStringFlush()
                    #
                    self.state = self.State.COLLECT_TRANSLATE
                    #
                    return

            if self.WAIT_TAG_DIMGRAY_AS_EXAMPLE_SIGN and text == "c dimgray":
                self.state = self.State.COLLECT_EXAMPLE
                #
                return

            if text == "ex":
                if self.state == self.State.COLLECT_IDIOMS:
                    pass
                else:
                    self.state = self.State.COLLECT_EXAMPLE
                #
                return

            if text == "m1" or text == "m2" or text == "m3" or text == "m4":
                if self.state == self.State.WAIT_INFO:
                    return

                #
                self.currentStringFlush()

                #
                if self.state == self.State.COLLECT_IDIOMS:
                    pass
                else:
                    self.state = self.State.COLLECT_TRANSLATE
                #
                return

            if text == "diamond":
                # В ряде статей встречается ссылка на раздел с идиомами,
                # путем помещения символа diamond после выражениия "cм. тж."
                if not self.currentStringRaw.strip().endswith("[см. тж."):
                    self.state = self.State.COLLECT_IDIOMS
                #
                return

            #
            return

        if tokenType == "tag-close":

            self.tagsPop(text)

            if text == "p":
                #
                self.stopPometa()

                #
                return

            if self.KZ_DICT_POMETA_MODE and text == "c":
                #
                self.stopPometa()

                #
                return

            if text == "m":
                self.currentStringFlush()

                #
                if self.state == self.State.COLLECT_IDIOMS:
                    pass
                else:
                    self.state = self.State.WAIT_TRANSLATE

                #
                return

            if text == "ex":
                #
                self.currentStringFlush()

                #
                if self.state == self.State.COLLECT_IDIOMS:
                    pass
                else:
                    self.currentExample = None
                    self.state = self.State.WAIT_TRANSLATE

                #
                return

            if text == "trn":
                #
                self.currentStringFlush()

                #
                self.state = self.State.WAIT_INFO

                #
                return

            #
            return

        #
        # Пока собираем блок
        #
        if tokenType == "text":
            #
            self.currentStringCollect(text)

        if self.state == self.State.WAIT_WORD:
            #
            self.currentStringFlush()

            #
            self.state = self.State.WAIT_INFO

            #
            return
