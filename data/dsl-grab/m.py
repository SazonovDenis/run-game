from Parser import ParserPrinter
from ParserDict import ParserDict
from ParserLex import ParserLex
from ParserStr import ParserStr

#inFileName = "/home/dvsa/projects/jc2-projects/run-game/data/dsl-grab/__En-Ru-Apresyan.dsl"
inFileName = "/home/dvsa/projects/jc2-projects/run-game/data/dsl-grab/__Ru-En-Smirnitsky.dsl"
outDirName = "/home/dvsa/projects/jc2-projects/run-game/data/dsl-grab/out/"
#inFileName = "_En-Ru-Apresyan-t.dsl"
#inFileName = "_En-Ru-Apresyan-out.dsl"
#inFileName = "_En-Ru-Apresyan-can.dsl"

###
parserPrint = ParserPrinter()

parserDict = ParserDict()
parserDict.onToken = parserPrint
parserDict.outDirName = outDirName

parserLex = ParserLex()
parserLex.onToken = parserDict

parserStr = ParserStr()
parserStr.onToken = parserLex

###
with open(inFileName, 'r', encoding="utf-8") as inFile:
    while True:
        line = inFile.readline()
        if line == '':
            break

        # print("===")
        #print("str: " + line, end="")
        # print("---")
        parserStr.next(line, None)
