import os

from Parser import ParserBase
from db.DbConst import FactType, TagType, TagValue


class ItemSaver(ParserBase):
    outDirName = None
    #
    csvItem = None
    csvFact = None
    csvItemTag = None
    csvFactTag = None
    errors = None
    #
    idItem = 0
    idFact = 0
    idItemTag = 0
    idFactTag = 0

    def handle(self, token, tokenType):
        print(token["text"])
        self.writeToken(token)

    def open(self, outDirName):
        self.outDirName = outDirName

        os.makedirs(self.outDirName, exist_ok=True)

        self.csvItem = open(self.outDirName + "Item.csv", "w", encoding="utf-8")
        self.csvFact = open(self.outDirName + "Fact.csv", "w", encoding="utf-8")
        self.csvItemTag = open(self.outDirName + "ItemTag.csv", "w", encoding="utf-8")
        self.csvFactTag = open(self.outDirName + "FactTag.csv", "w", encoding="utf-8")
        self.errors = open(self.outDirName + "errors.txt", "w", encoding="utf-8")

        # self.csvItem.write("id\tvalue" + "\n")
        # self.csvFact.write("id\titem\tfactType\tvalue" + "\n")
        # self.csvItemTag.write("id\titem\tfact\ttagType\ttagValue" + "\n")
        # self.csvFactTag.write("id\tfact\ttagType\ttagValue" + "\n")

        print("out directory: " + self.outDirName)
        print()

    def close(self):
        self.csvItem.close()
        self.csvFact.close()
        self.csvItemTag.close()
        self.csvFactTag.close()
        self.errors.close()

        print()
        print("write done")

    def writeToken(self, token):
        #
        self.idItem = self.idItem + 1
        self.csvItem.write(str(self.idItem) + "\t" + token["text"] + "\n")
        self.idItemTag = self.idItemTag + 1
        self.csvItemTag.write(str(self.idItemTag) + "\t" + str(self.idItem) + "\t" + TagType.word_lang + "\t" + TagValue.eng + "\n")

        #
        self.idFact = self.idFact + 1
        self.csvFact.write(str(self.idFact) + "\t" + str(self.idItem) + "\t" + "\\N" + "\t" + FactType.spelling + "\t" + token["text"] + "\n")
        self.idFactTag = self.idFactTag + 1
        self.csvFactTag.write(str(self.idFactTag) + "\t" + str(self.idFact) + "\t" + TagType.word_lang + "\t" + TagValue.eng + "\n")

        #
        if token.get("transcription") != None:
            self.idFact = self.idFact + 1
            self.csvFact.write(str(self.idFact) + "\t" + str(self.idItem) + "\t" + "\\N" + "\t" + FactType.transcription + "\t" + token["transcription"] + "\n")

        #
        idioms = token["idioms"]
        for idiom in idioms:
            idiom_text = idiom["text"]
            if len(idiom_text) == 0:
                continue
            if len(idiom_text) > 1000:
                self.errors.write("idiom too long, item: " + token["text"] + "\n")
                self.errors.write(idiom_text + "\n")
                self.errors.write("\n")
                continue

            self.idFact = self.idFact + 1
            self.csvFact.write(str(self.idFact) + "\t" + str(self.idItem) + "\t" + "\\N" + "\t" + FactType.idiom + "\t" + idiom_text + "\n")

        #
        for translation in token["translations"]:
            translation_text = translation["text"]
            if (translation_text == ""):
                continue

            #
            self.idFact = self.idFact + 1
            self.csvFact.write(str(self.idFact) + "\t" + str(self.idItem) + "\t" + "\\N" + "\t" + FactType.translate + "\t" + translation_text + "\n")
            self.idFactTag = self.idFactTag + 1
            self.csvFactTag.write(str(self.idFactTag) + "\t" + str(self.idFact) + "\t" + TagType.translate_direction + "\t" + TagValue.eng_rus + "\n")
            self.idFactTag = self.idFactTag + 1
            self.csvFactTag.write(str(self.idFactTag) + "\t" + str(self.idFact) + "\t" + TagType.dictionary + "\t" + TagValue.dictionary_full + "\n")
            #
            idFactRoot = self.idFact

            #
            for example in translation["examples"]:
                example_text = example["text"]
                if len(example_text) > 1000:
                    self.errors.write("example too long, item: " + token["text"] + "\n")
                    self.errors.write(example_text + "\n")
                    self.errors.write("\n")
                    continue
                self.idFact = self.idFact + 1
                self.csvFact.write(str(self.idFact) + "\t" + str(self.idItem) + "\t" + str(idFactRoot) + "\t" + FactType.example + "\t" + example_text + "\n")
