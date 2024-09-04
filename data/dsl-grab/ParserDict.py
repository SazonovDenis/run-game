from enum import IntEnum

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

    def __init__(self):
        self.state = self.State.WAIT_WORD
        self.token = None
        self.tokenType = None

    def cleanupText(self, text):
        text = text.strip()

        cleanSymbols = " 1234567890.)"
        while len(text) > 0:
            if cleanSymbols.find(text[0:1]) != -1:
                text = text[1:]
            else:
                break

        return text

    def cleanupData(self, token):
        translations = token["translations"]
        translationsLen = len(translations)
        for translation_idx in range(translationsLen - 1, -1, -1):
            translation = translations[translation_idx]
            examples = translation["examples"]

            #
            translation_text = translation["text"]

            translation_text = self.cleanupText(translation_text)
            if (translation_text == "" and len(examples) == 0):
                translations.pop(translation_idx)
                continue

            translation["text"] = translation_text

            #
            for example in examples:
                example_text = example["text"].strip()
                example["text"] = example_text

    def clearToken(self):
        self.token = None
        self.tokenType = None
        self.currentTranslations = None

    def flushToken(self):
        if self.token != None:
            self.cleanupData(self.token)
            #
            self.onToken(self.token, self.tokenType)
            #
            self.clearToken()

    def createToken(self, text):
        self.token = {}
        self.tokenType = "item"
        self.token["text"] = self.cleanupText(text)
        self.token["idioms"] = []
        self.token["translations"] = []
        self.currentTranslation = None

    def setTranscription(self, text):
        self.token["transcription"] = self.cleanupText(text)

    def addTranslation(self, text):
        translation = {}
        translation["text"] = self.cleanupText(text)
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
        example["text"] = self.cleanupText(text)
        example["tags"] = self.currentPometas
        self.currentTranslation["examples"].append(example)

    def addIdiom(self, text):
        idiom = {}
        idiom["text"] = self.cleanupText(text)
        self.token["idioms"].append(idiom)

    def currentStringFlush(self):
        currentString = self.currentString.strip()
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
