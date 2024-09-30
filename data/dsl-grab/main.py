from ItemPrinter import ItemPrinter
from ItemSaver import ItemSaver
from ParserDict import ParserDict
from ParserLex import ParserLex
from ParserStr import ParserStr
from db import DbConst

outDirName = "/home/dvsa/projects/jc2-projects/run-game/data/dsl-grab/out/"
# inFileName = "/home/dvsa/projects/jc2-projects/run-game/data/dsl-grab/__En-Ru-Apresyan.dsl"
# inFileName = "/home/dvsa/projects/jc2-projects/run-game/data/dsl-grab/__Ru-En-Smirnitsky.dsl"
inFileName = "/home/dvsa/projects/jc2-projects/run-game/data/dsl-grab/__kaz-rus_Kazakh_v1_1.dsl"
# inFileName = "_En-Ru-Apresyan-bar.dsl"
# inFileName = "_En-Ru-Apresyan-arm.dsl"
# inFileName = "_En-Ru-Apresyan-test.dsl"
# inFileName = "_En-Ru-Apresyan-diameter.dsl"
# inFileName = "_En-Ru-Apresyan-out.dsl"
# inFileName = "_En-Ru-Apresyan-can.dsl"

###
itemSaver = ItemSaver()
itemSaver.tagValue_word_lang = DbConst.TagValue.kaz
itemSaver.tagValue_translate_direction = DbConst.TagValue.kaz_rus
itemSaver.open(outDirName)

itemPrinter = ItemPrinter()
itemPrinter.printSamples = False

parserDict = ParserDict()
# parserDict.nextParser = itemPrinter
parserDict.nextParser = itemSaver

#
parserLex = ParserLex()
parserLex.nextParser = parserDict

parserStr = ParserStr()
parserStr.nextParser = parserLex

# Настройка парсера для казахского словаря
parserDict.WAIT_TAG_M2_AS_TRASLATION = True
parserDict.WAIT_TAG_DIMGRAY_AS_EXAMPLE_SIGN = True
parserDict.KZ_DICT_EXAMPLE_MODE = True
parserDict.KZ_DICT_POMETA_MODE = True

###
with open(inFileName, 'r', encoding="utf-8") as inFile:
    while True:
        line = inFile.readline()
        if line == '':
            break

        #
        parserStr.handle(line, None)

###
itemSaver.close()
