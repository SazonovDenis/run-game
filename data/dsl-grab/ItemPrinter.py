from Parser import ParserBase


class ItemPrinter(ParserBase):
    printSamples = True

    def handle(self, token, tokenType):
        self.printToken(token)

    def printToken(self, token):
        print("------ token ----------")
        if token.get("transcription") == None:
            print(token["text"] + " <no transcription>")
        else:
            print(token["text"] + " " + token["transcription"])

        idioms = token["idioms"]
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
        for translation in token["translations"]:
            tn = tn + 1
            print("  [" + str(tn) + "] " + translation["text"])

            translationTags = translation["tags"]
            if len(translationTags) != 0:
                print("      tags: ", end="")
                for tag in translationTags:
                    print("{" + tag + "}", end="")
                print("")

            if self.printSamples:
                examples = translation["examples"]
                if len(examples) != 0:
                    en = 0
                    for example in translation["examples"]:
                        en = en + 1
                        print("      smp[" + str(en) + "] " + example["text"])

                        exampleTags = example["tags"]
                        if len(exampleTags) > 0:
                            print("         tags: ", end="")
                            for tag in exampleTags:
                                print("{" + tag + "}", end="")
                            print("")

        print("------ token end ------")
        print("")
