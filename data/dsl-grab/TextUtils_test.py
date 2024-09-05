import TextUtils as textUtils


def test0(text):
    print()
    print("              text: " + text)
    print("  cleanupTextLevel: " + textUtils.cleanNumberLevel(text))
    print("getTextLevelNumber: " + str(textUtils.getTextLevelNumber(text)))
    print("     getTextLevels: " + textUtils.getTextLevels(text))
    print(" cleanupTextNumber: " + textUtils.cleanNumber(text))
    print("     popTextNumber: " + textUtils.popTextNumber(text))


def test1(text):
    print()
    print("             text: " + text)
    print("     textToNumber: " + textUtils.textToNumber(text))


test0("")
test0("I dklsd")
test0("II nmsd e")
test0("IV qkwqjw qwkkre")
test0("4 four")
test0("1. one")
test0("1. 3) кусок, брусок")
test0("3) болванка, чушка")
test0("7) = crow")
test0("2. 1) перекладина")
test0("1. 2) перекладина")

test1("")
test1("0")
test1("2")
test1("56")
test1("032")
test1("I")
test1("I")
test1("IX")
