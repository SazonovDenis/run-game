from Parser import ParserPrinter
from ParserDict import ParserDict
from ParserLex import ParserLex
from ParserStr import ParserStr

inFileName = "/home/dvsa/projects/jc2-projects/run-game/t/_En-Ru-Apresyan-t.dsl"

###
parserPrint = ParserPrinter()

parserLex = ParserLex()
parserLex.onToken = parserPrint

parserStr = ParserStr()
parserStr.onToken = parserLex

###
with open(inFileName, 'r', encoding="utf-8") as inFile:
    while True:
        line = inFile.readline()
        if line == '':
            break

        print("===")
        print(line, end="")
        print("---")
        parserStr.next(line, None)
