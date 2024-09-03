from enum import IntEnum

from Parser import ParserBase
from db.ItemSaver import ItemSaver


class ParserDict(ParserBase):
    class State(IntEnum):
        WAIT_WORD = 1
        WAIT_INFO = 2
        WAIT_TRANSLATE = 3
        COLLECT_EXAMPLE = 4
        COLLECT_TRANSLATE = 5
        COLLECT_IDIOMS = 6

    tags = {}
    currentTags = None
    currentTranslation = None
    currentExample = None

    outDirName = None

    itemSaver = None

    def __init__(self):
        self.state = self.State.WAIT_WORD
        self.token = None
        self.tokenType = None

    def open(self, outDirName):
        self.itemSaver = ItemSaver()
        self.itemSaver.open(outDirName)

    def close(self):
        self.itemSaver.close()

    def printToken(self):
        print("-----------")
        if self.token.get("transcription") == None:
            print(self.token["text"] + " <no transcription>")
        else:
            print(self.token["text"] + " " + self.token["transcription"])

        idioms = self.token["idioms"]
        if len(idioms) != 0:
            print("idioms:")
            idn = 0
            for idiom in idioms:
                if (idiom["text"] == ""):
                    continue
                idn = idn + 1
                print("  [" + str(idn) + "] " + idiom["text"])
            print("translations:")

        tn = 0
        for translation in self.token["translations"]:
            if (translation["text"] == ""):
                continue

            tn = tn + 1

            print("  [" + str(tn) + "] " + translation["text"])

            translationTagsKeys = translation["tags"].keys()
            if len(translationTagsKeys) != 0:
                print("      ", end="")
                for tag in translationTagsKeys:
                    print("*" + tag + "* ", end="")
                print("")

            examples = translation["examples"]
            if len(examples) != 0:
                # print("  examples:")
                en = 0
                for example in translation["examples"]:
                    en = en + 1
                    print("      s[" + str(en) + "] " + example["text"])
        print("===========")
        print("")

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
        self.token["idioms"] = []
        self.currentTranslations = None

    def flushToken(self):
        if self.token != None:
            self.itemSaver.printToken(self.token)
            print(self.token["text"])
            # self.printToken()

            self.clearToken()
            self.clearTags()

    def newExample(self):
        self.currentExample = {}
        self.currentExample["text"] = ""
        self.currentTranslation["examples"].append(self.currentExample)

    def newTranslate(self):
        self.currentTags = {}
        self.currentTranslation = {}
        self.currentTranslation["text"] = ""
        self.currentTranslation["tags"] = self.currentTags
        self.currentTranslation["examples"] = []
        self.currentExample = None
        #
        self.token["translations"].append(self.currentTranslation)

    def newIdiom(self):
        self.currentIdiom = {}
        self.currentIdiom["text"] = ""
        self.token["idioms"].append(self.currentIdiom)

    def next(self, text, tokenType):
        textStrip = text.strip()

        if tokenType == "new-line":
            self.flushToken()
            self.state = self.State.WAIT_WORD
            #
            return

        if tokenType == "tag":
            self.tags[text] = True

            if text == "trn":
                self.tags = {}
                #
                self.state = self.State.WAIT_TRANSLATE
                #
                return

            if text == "ex":
                if self.state == self.State.COLLECT_IDIOMS:
                    self.newIdiom()
                else:
                    self.newExample()
                    #
                    self.state = self.State.COLLECT_EXAMPLE
                #
                return

            if text == "m1" or text == "m2":
                if self.state == self.State.COLLECT_IDIOMS:
                    self.newIdiom()
                else:
                    self.newTranslate()
                    #
                    self.state = self.State.COLLECT_TRANSLATE
                #
                return

            if text == "diamond":
                if (self.currentExample == None) or (not self.currentExample["text"].strip().endswith("[см. тж.")):
                    self.newIdiom()
                    #
                    self.state = self.State.COLLECT_IDIOMS
                #
                return

            #
            return

        if tokenType == "tag-close":
            self.tags.pop(text, "")

            if text == "m":
                self.tags.pop("m1", "")
                self.tags.pop("m2", "")
                self.tags.pop("m3", "")
                self.tags.pop("m4", "")

                #
                if self.state == self.State.COLLECT_IDIOMS:
                    pass
                else:
                    self.state = self.State.WAIT_TRANSLATE

                #
                return

            if text == "ex":
                if self.state == self.State.COLLECT_IDIOMS:
                    pass
                else:
                    self.currentExample = None
                    self.state = self.State.WAIT_TRANSLATE

                #
                return

            #
            return

        if self.state == self.State.WAIT_WORD:
            if len(textStrip) > 0:
                self.clearTags()
                self.newToken()
                self.token["text"] = textStrip
                #
                self.state = self.State.WAIT_INFO

            #
            return

        if self.state == self.State.WAIT_INFO:
            if tokenType == "text":

                if len(self.tags.keys()) == 0 and len(textStrip) != 0:
                    self.token["transcription"] = textStrip
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

        if self.state == self.State.COLLECT_TRANSLATE:
            if self.tags.get("com", None) == None:
                self.currentTranslation["text"] = self.currentTranslation["text"] + text
            else:
                if len(textStrip) == 0:
                    self.currentTranslation["text"] = self.currentTranslation["text"] + text
                else:
                    self.currentTranslation["text"] = self.currentTranslation["text"] + "**" + text + "**"
                    #
                    self.currentTags[textStrip] = True
            #
            return

        if self.state == self.State.COLLECT_EXAMPLE:
            self.currentExample["text"] = self.currentExample["text"] + text
            #
            return

        if self.state == self.State.COLLECT_IDIOMS:
            self.currentIdiom["text"] = self.currentIdiom["text"] + text
            #
            return
