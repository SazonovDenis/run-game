from ItemPrinter import ItemPrinter
from ItemSaver import ItemSaver
from ParserDict import ParserDict
from ParserLex import ParserLex
from ParserStr import ParserStr


###
def convert(inFileName, outDirName):
    print(inFileName + " -> " + outDirName)

    itemSaver = ItemSaver()
    itemSaver.open(outDirName)

    itemPrinter = ItemPrinter()

    parserDict = ParserDict()
    # parserDict.nextParser = itemPrinter
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


###
outDirRoot = "/home/dvsa/projects/jc2-projects/run-game/data/dsl-grab/out/"
inDirRoot = "/home/dvsa/projects/jc2-projects/run-game/data/dsl-grab/"

#
outDirName = outDirRoot + "eng-rus/"
inFileName = inDirRoot + "__En-Ru-Apresyan.dsl"
convert(inFileName, outDirName)

exit(0)

#
outDirName = outDirRoot + "rus-eng/"
inFileName = inDirRoot + "__Ru-En-Smirnitsky.dsl"
convert(inFileName, outDirName)

#
outDirName = outDirRoot + "kaz-rus/"
inFileName = inDirRoot + "__kaz-rus_Kazakh_v1_1.dsl"
convert(inFileName, outDirName)

#
outDirName = outDirRoot + "rus-kaz/"
inFileName = inDirRoot + "__rus-kaz_Kazakh_v1_1.dsl"
convert(inFileName, outDirName)
