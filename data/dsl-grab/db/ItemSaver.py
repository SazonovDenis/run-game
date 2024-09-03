from db.DbConst import FactType, TagType, TagValue


class ItemSaver:
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

    def open(self, outDirName):
        self.outDirName = outDirName

        self.csvItem = open(self.outDirName + "Item.csv", "w", encoding="utf-8")
        self.csvFact = open(self.outDirName + "Fact.csv", "w", encoding="utf-8")
        self.csvItemTag = open(self.outDirName + "ItemTag.csv", "w", encoding="utf-8")
        self.csvFactTag = open(self.outDirName + "FactTag.csv", "w", encoding="utf-8")
        self.errors = open(self.outDirName + "errors.txt", "w", encoding="utf-8")

        # self.csvItem.write("id\tvalue" + "\n")
        # self.csvFact.write("id\titem\tfactType\tvalue" + "\n")
        # self.csvItemTag.write("id\titem\ttagType\ttagValue" + "\n")
        # self.csvFactTag.write("id\tfact\ttagType\ttagValue" + "\n")

    def close(self):
        self.csvItem.close()
        self.csvFact.close()
        self.csvItemTag.close()
        self.csvFactTag.close()
        self.errors.close()

    def printToken(self, token):
        self.idItem = self.idItem + 1
        self.csvItem.write(str(self.idItem) + "\t" + token["text"] + "\n")
        self.idItemTag = self.idItemTag + 1
        self.csvItemTag.write(str(self.idItemTag) + "\t" + str(self.idItem) + "\t" + TagType.word_lang + "\t" + TagValue.eng + "\n")

        self.idFact = self.idFact + 1
        self.csvFact.write(str(self.idFact) + "\t" + str(self.idItem) + "\t" + FactType.spelling + "\t" + token["text"] + "\n")

        if token.get("transcription") != None:
            self.idFact = self.idFact + 1
            self.csvFact.write(str(self.idFact) + "\t" + str(self.idItem) + "\t" + FactType.transcription + "\t" + token["transcription"] + "\n")

        idioms = token["idioms"]
        for idiom in idioms:
            if (idiom["text"] == ""):
                continue
            if len(idiom["text"]) > 500:
                self.errors.write("text too long, item: " + token["text"] + "\n")
                self.errors.write("idiom: " + idiom["text"] + "\n")
                self.errors.write("\n")
                continue

            self.idFact = self.idFact + 1
            self.csvFact.write(str(self.idFact) + "\t" + str(self.idItem) + "\t" + FactType.idiom + "\t" + idiom["text"] + "\n")

        for translation in token["translations"]:
            if (translation["text"] == ""):
                continue

            self.idFact = self.idFact + 1
            self.csvFact.write(str(self.idFact) + "\t" + str(self.idItem) + "\t" + FactType.translate + "\t" + translation["text"] + "\n")
            self.idFactTag = self.idFactTag + 1
            self.csvFactTag.write(str(self.idFactTag) + "\t" + str(self.idFact) + "\t" + TagType.word_translate_direction + "\t" + TagValue.eng_rus + "\n")

            for example in translation["examples"]:
                if len(example["text"]) > 500:
                    self.errors.write("text too long, item: " + token["text"] + "\n")
                    self.errors.write("example: " + example["text"] + "\n")
                    self.errors.write("\n")
                    continue
                self.idFactTag = self.idFactTag + 1
                self.csvFactTag.write(str(self.idFactTag) + "\t" + str(self.idFact) + "\t" + TagType.word_use_sample + "\t" + example["text"] + "\n")
