from ItemPrinter import ItemPrinter
from ItemSaver import ItemSaver
from ParserDict import ParserDict
from ParserLex import ParserLex
from ParserStr import ParserStr

outDirName = "/home/dvsa/projects/jc2-projects/run-game/data/dsl-grab/out/"
# inFileName = "/home/dvsa/projects/jc2-projects/run-game/data/dsl-grab/__En-Ru-Apresyan.dsl"
# inFileName = "/home/dvsa/projects/jc2-projects/run-game/data/dsl-grab/__Ru-En-Smirnitsky.dsl"
# inFileName = "_En-Ru-Apresyan-bar.dsl"
# inFileName = "_En-Ru-Apresyan-arm.dsl"
# inFileName = "_En-Ru-Apresyan-test.dsl"
# inFileName = "_En-Ru-Apresyan-diameter.dsl"
# inFileName = "_En-Ru-Apresyan-out.dsl"
inFileName = "_En-Ru-Apresyan-can.dsl"

###
itemSaver = ItemSaver()
itemSaver.open(outDirName)

itemPrinter = ItemPrinter()
itemPrinter.printSamples = False

parserDict = ParserDict()
#parserDict.nextParser = itemPrinter
parserDict.nextParser = itemSaver

parserLex = ParserLex()
parserLex.nextParser = parserDict

parserStr = ParserStr()
parserStr.nextParser = parserLex

###
with open(inFileName, 'r', encoding="utf-8") as inFile:
    while True:
        line = inFile.readline()
        if line == '':
            break

        # print("===")
        # print("str: " + line, end="")
        # print("---")
        parserStr.handle(line, None)

###
itemSaver.close()
