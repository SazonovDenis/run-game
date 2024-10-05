class LangDetector:
    langs = []
    words = {}
    wordItemCount = {}
    wordCount = {}

    def cleanWord(self, word):
        word = word.replace("<<", "")

        word = word.replace(">>", "")
        word = word.strip(" ,;-\n")

        return word

    def containsSymbolKaz(self, word):
        kz = "әғқңөұүhіүқ"
        for char in word:
            if kz.find(char) != -1:
                return True

        return False

    def getLangs(self, word):
        res = {}
        for lang in self.langs:

            words = self.words[lang]
            wordItem = words.get(word, None)

            if wordItem != None:
                res[lang] = wordItem
            else:
                res[lang] = 0

        return res

    def getLangsFull(self, word):
        res = self.getLangs(word)

        while res["kaz"] == 0 and res["rus"] == 0 and len(word) > 2:
            word = word[0:-1]
            res = self.getLangs(word)

        return res

    def getLang(self, word):
        word = langDetector.cleanWord(word)
        res = langDetector.getLangs(word)

        # ---
        if res["kaz"] > 0 and res["rus"] == 0:
            return "kaz"

        if res["rus"] > 0 and res["kaz"] == 0:
            return "rus"

        if res["rus"] > 0 and res["kaz"] > 0:
            if res["rus"] / res["kaz"] > 1.2:
                return "rus"
            if res["kaz"] / res["rus"] > 1.2:
                return "kaz"

        if langDetector.containsSymbolKaz(word):
            return "kaz"

        # ---
        res = langDetector.getLangsFull(word)

        if res["kaz"] > 0 and res["rus"] == 0:
            return "kaz"

        if res["rus"] > 0 and res["kaz"] == 0:
            return "rus"

        if res["rus"] > 0 and res["kaz"] > 0:
            if res["rus"] / res["kaz"] > 1.2:
                return "rus"
            if res["kaz"] / res["rus"] > 1.2:
                return "kaz"

        # ---
        return None

    def addWord(self, word, lang):
        if not lang in self.langs:
            self.langs.append(lang)
            self.words[lang] = {}
            self.wordItemCount[lang] = 0
            self.wordCount[lang] = 0

        #
        words = self.words[lang]
        itemCount = self.wordItemCount[lang]
        wordCount = self.wordCount[lang]

        #
        wordItem = words.get(word, None)

        #
        if wordItem == None:
            wordItem = 1
            wordCount = wordCount + 1
        else:
            wordItem = wordItem + 1
        itemCount = itemCount + 1

        #
        words[word] = wordItem

        #
        self.wordItemCount[lang] = itemCount
        self.wordCount[lang] = wordCount

    def loadFromFile(self, fileName, lang):
        print(fileName)

        with open(fileName, 'r', encoding="utf-8") as inFile:
            while True:
                line = inFile.readline()
                if line == '':
                    break

                line = line.strip().lower()
                for word in line.split(" "):
                    if len(word) > 0:
                        langDetector.addWord(word, lang)
            #
            inFile.close()


#
langDetector = LangDetector()

#
fileName = "./LangDetector.rus.txt"
print(fileName)

with open(fileName, 'r', encoding="utf-8") as inFile:
    while True:
        line = inFile.readline()
        if line == '':
            break

        word = line.strip()
        if len(word) > 0:
            langDetector.addWord(word, "rus")
    #
    inFile.close()

#
fileName = "./LangDetector.rus.1.txt"
langDetector.loadFromFile(fileName, "rus")
#
fileName = "./LangDetector.rus.2.txt"
langDetector.loadFromFile(fileName, "rus")

#
fileName = "../web-grab/word_synonyms.rus.txt"
print(fileName)

with open(fileName, 'r', encoding="utf-8") as inFile:
    while True:
        line = inFile.readline()
        if line == '':
            break

        # pos = line.find("|")
        # word = line[0:pos]
        words = line.split("|")
        for word in words:
            word = word.strip().lower()
            if len(word) > 0:
                langDetector.addWord(word, "rus")

    #
    inFile.close()

#
fileName = "../dsl-grab/__kaz-rus_Kazakh_v1_1.dsl"
print(fileName)

with open(fileName, 'r', encoding="utf-8") as inFile:
    while True:
        line = inFile.readline()
        if line == '':
            break

        if line.startswith("\t"):
            continue

        word = line.strip().lower()
        if len(word) > 0:
            langDetector.addWord(word, "kaz")

    #
    inFile.close()

#
fileName = "./LangDetector.kaz.1.txt"
langDetector.loadFromFile(fileName, "kaz")
#
fileName = "./LangDetector.kaz.2.txt"
langDetector.loadFromFile(fileName, "kaz")
#
fileName = "./LangDetector.kaz.3.txt"
langDetector.loadFromFile(fileName, "kaz")

# Обрабатываем материал
fileName = "../dsl-grab/out/kaz-rus/Fact.csv"
fileNameOut = "../dsl-grab/out/kaz-rus/Fact1.csv"
print(fileName)

#
count = 0
countBad = 0

outFile = open(fileNameOut, 'w', encoding="utf-8")

#
inFile = open(fileName, 'r', encoding="utf-8")
while True:
    line = inFile.readline()
    if line == '':
        break

    lineValues = line.split("\t")
    factType = int(lineValues[3])
    textExample = lineValues[4].replace("@", " ").strip()

    if factType == 1007:
        words = textExample.split(" ")

        # if lineValues[0] == "3000036":
        #    print(line)
        #    pass

        #
        idxKaz = 0
        idxRus = 0

        # Определим posKaz
        idx = 0
        while idx < len(words):
            word = words[idx]
            lang = langDetector.getLang(word)
            if lang == "rus":
                break
            if lang == None and idxKaz >= 2:
                break
            if lang == "kaz":
                idxKaz = idx
            #
            idx = idx + 1

        # Определим posRus
        maxPos = len(words) - 1
        idx = maxPos
        while idx >= 0:
            word = words[idx]
            lang = langDetector.getLang(word)
            if lang == "kaz":
                break
            if lang == None and idx <= idxKaz:
                break
            if lang == "rus":
                idxRus = idx
            #
            idx = idx - 1

        # Поставим разделитель
        idx = 0
        text = ""
        while idx < len(words):
            word = words[idx]
            word = langDetector.cleanWord(word)
            #
            if idx == idxRus:
                text = text + " rus>>"
            #
            text = text + " " + word
            #
            if idx == idxKaz:
                text = text + " <<kaz"
            #
            idx = idx + 1
        #
        text = text.strip()

        #
        count = count + 1

        #
        textExampleNew = ""
        if idxKaz + 1 == idxRus:
            p = 0
            idx = 0
            for word in words:
                if idx == idxRus:
                    break

                idx = idx + 1
                p = textExample.find(word, p)
                p = p + len(word)

            #
            textExampleNew = textExample[0:p].strip() + " -- " + textExample[p:].strip()
            line = lineValues[0] + "\t" + lineValues[1] + "\t" + lineValues[2] + "\t" + lineValues[3] + "\t" + textExampleNew + "\n"

        #
        if idxKaz + 1 != idxRus:
            #
            countBad = countBad + 1
            print(str(countBad) + " / " + str(count))
            #
            for word in words:
                word = langDetector.cleanWord(word)
                if len(word) > 0:
                    res = langDetector.getLangs(word)
                    print(word + " " + str(res), end=" ")
            print("")
            #
            print(line)
            print(text)
            print("")

    #
    outFile.write(line)

#
outFile.close()
inFile.close()
