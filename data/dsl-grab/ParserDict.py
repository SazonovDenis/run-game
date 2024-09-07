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

    modePometa = False
    currentPometas = []

    groupTags = None
    groupLevel = None
    groupLevelNumber = None

    def __init__(self):
        self.state = self.State.WAIT_WORD
        self.token = None
        self.tokenType = None

    def deletePometas(self, text):
        while len(text) > 0:
            n0 = text.find("{")
            n1 = text.find("}")
            if n0 == -1:
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
            if n0 != 0:
                break
            text = text[0:n0] + text[n1 + 1:]

        #
        text = text.strip()

        return text

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
            # Уберем помены спереди
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
        s1 = "әғқңөұүhіүқ"
        s2 = "эгкноуухиук"

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

    def currentStringFlush(self):
        currentString = self.currentString.replace("\xa0", " ").strip()
        if currentString == "":
            return

        # print("[" + str(self.state)[6:].ljust(18, ' ') + "]: " + currentString)

        if self.state == self.State.WAIT_WORD:
            self.createToken(currentString)
            pass
        elif self.state == self.State.WAIT_INFO:
            self.setTranscription(currentString)
            pass
        elif self.state == self.State.COLLECT_TRANSLATE:
            self.addTranslation(currentString)
            pass
        elif self.state == self.State.COLLECT_EXAMPLE:
            self.addExample(currentString)
            pass
        elif self.state == self.State.COLLECT_IDIOMS:
            self.addIdiom(currentString)
            pass

        #
        self.currentString = ""
        self.currentStringRaw = ""
        self.currentPometas = []

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
        if len(self.currentPometa) != 0:
            self.currentPometas.append(self.currentPometa)
        #
        self.modePometa = False
        self.currentPometa = None

    def handle(self, text, tokenType):

        if tokenType == "new-line":
            self.flushToken()
            #
            self.state = self.State.WAIT_WORD
            #
            return

        if tokenType == "tag":

            if text == "p":
                self.startPometa()

                #
                return

            if text == "trn":
                self.currentStringFlush()
                #
                self.state = self.State.WAIT_TRANSLATE
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

            if text == "p":
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
