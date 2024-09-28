# Удаляет всю нумерацию (цифры и скобки) в начале строки
def cleanNumberLevel(text):
    text = text.strip()

    cleanSymbols = " 1234567890.)IV-"
    while len(text) > 0:
        if cleanSymbols.find(text[0:1]) != -1:
            text = text[1:]
        else:
            break

    return text


# Удаляет все цифры в начале строки
def cleanNumber(text):
    text = text.strip()

    cleanSymbols = " 1234567890IVX"
    while len(text) > 0:
        if cleanSymbols.find(text[0:1]) != -1:
            text = text[1:]
        else:
            break

    return text


def startsWithNumber(text):
    text = text.strip()

    numberSymbols = "1234567890IVX"

    if numberSymbols.find(text[0:1]) != -1:
        return True
    else:
        return False


def popTextNumber(text):
    text = text.strip()

    numberSymbols = "1234567890IVX"

    num = ""
    while len(text) > 0:
        if numberSymbols.find(text[0:1]) != -1:
            num = num + text[0:1]
            text = text[1:]
        else:
            break

    return num


# Текст в число и обратно.
# Должен быть арабскими или римскими цифрами, без лишних символов
def textToNumber(text):
    if len(text) == 0:
        return ""

    numberSymbols = "1234567890"
    if numberSymbols.find(text[0:1]) != -1:
        return str(int(text))

    numbersRoma = ["I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"]
    for n in range(0, len(numbersRoma)):
        numberRoma = numbersRoma[n]
        if text == numberRoma:
            return str(n + 1)

    return ""


def getTextLevelNumber(text):
    numbersRomaSymbols = "IVX"

    number = popTextNumber(text)

    if number == "":
        return (0, "")

    level = 0
    if numbersRomaSymbols.find(number[0:1]) != -1:
        level = 1
    else:
        firstSymbolAfterNumber = text[len(number):len(number) + 1]
        if (firstSymbolAfterNumber == "."):
            level = 2
        elif (firstSymbolAfterNumber == ")"):
            level = 3

    #
    number = textToNumber(number)

    #
    return (level, number)


def getTextLevels(text):
    levelsText = ""

    #
    level = 0

    while len(text) > 0:
        (numberLevel, number) = getTextLevelNumber(text)

        if number == "":
            break

        #
        if level > numberLevel:
            raise "numberLevel < level"

        #
        while level < numberLevel:
            level = level + 1
            levelsText = levelsText + "000" + "-"

        numberStr = number.rjust(3, "0")

        levelsText = levelsText + numberStr + "-"
        level = level + 1

        #
        text = text[len(number) + 1:].strip()

    #
    return levelsText
