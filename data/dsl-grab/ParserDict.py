from enum import IntEnum

from Parser import ParserBase


class ParserDict(ParserBase):
    class State(IntEnum):
        WAIT_WORD = 1
        WAIT_INFO = 2
        WAIT_TRANSLATE = 3

    tags = {}
    currentTranslation = None
    currentExample = None

    def __init__(self):
        self.state = self.State.WAIT_WORD
        self.token = None
        self.tokenType = None

    def clearToken(self):
        self.token = None
        self.tokenType = None
        self.currentTranslations = None

    def clearTags(self):
        self.tags = {}

    def newToken(self):
        self.token = {}
        self.tokenType = "item"
        self.token["translations"] = []
        self.currentTranslations = None

    def flushToken(self):
        if self.token != None:
            print("-----------")
            print(self.token["text"] + " " + self.token["transcription"])
            tn = 0
            for translation in self.token["translations"]:
                if (translation["text"] == ""):
                    continue

                tn = tn + 1
                print("  [" + str(tn) + "] " + translation["text"])

                examples = translation["examples"]
                if len(examples) != 0:
                    # print("  examples:")
                    en = 0
                    for example in translation["examples"]:
                        en = en + 1
                        print("      *(" + str(en) + ") " + example["text"])
            print("===========")
            self.clearToken()
            self.clearTags()

    def next(self, text, tokenType):

        if tokenType == "new-line":
            self.flushToken()
            self.state = self.State.WAIT_WORD
            #
            return

        elif tokenType == "tag":
            self.tags[text] = True

            if text == "trn":
                self.tags = {}
                #
                return

            if text == "m1":
                self.currentTranslation = {}
                self.currentTranslation["text"] = ""
                self.currentTranslation["examples"] = []
                self.currentExample = None
                #
                self.token["translations"].append(self.currentTranslation)
                #
                return

            #
            return

        elif tokenType == "tag-close":
            self.tags.pop(text, "")

            if text == "m":
                self.tags.pop("m1", "")
                self.tags.pop("m2", "")
                #
                return

            if text == "ex":
                self.currentExample = None
                #
                return

            #
            return

        if self.state == self.State.WAIT_WORD:
            if len(text) > 0:
                self.clearTags()
                self.newToken()
                self.token["text"] = text.strip()
                #
                self.state = self.State.WAIT_INFO

            #
            return

        if self.state == self.State.WAIT_INFO:
            if tokenType == "text":

                if len(self.tags.keys()) == 0:
                    self.token["transcription"] = text.strip()
                    #
                    self.state = self.State.WAIT_TRANSLATE
                    #
                    return

                else:
                    pass

                #
                return
            #
            return

        if self.state == self.State.WAIT_TRANSLATE:
            if tokenType == "text":

                if self.tags.get("m1") != None:
                    self.currentTranslation["text"] = self.currentTranslation["text"] + text
                    return

                elif self.tags.get("ex") != None:
                    if (self.currentExample == None):
                        self.currentExample = {}
                        self.currentExample["text"] = ""
                        self.currentTranslation["examples"].append(self.currentExample)
                    self.currentExample["text"] = self.currentExample["text"] + text
                    return

                else:
                    pass

                #
                return

            #
            return
