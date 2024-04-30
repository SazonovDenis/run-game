fn = "/home/dvsa/projects/jc2-projects/run-game/t/En-Ru-Apresyan.dsl"

###
file_dict = open(fn, 'r', encoding="utf-16")

###
STATE_NEW_WORD = 1
STATE_WORD_PARSE = 2

state = STATE_NEW_WORD
debug_print = False
word = ""
###

count = 0
while True:
    count += 1

    # Get next line from file
    s = file_dict.readline()

    # if line is empty
    # end of file is reached
    if not s:
        break

    ###
    s = s.strip()

    if s == "":
        state = STATE_NEW_WORD
        word = ""
    elif state == STATE_NEW_WORD:
        state = STATE_WORD_PARSE
        word = s
    else:
        pass

    ###
    if word == "closely":
        debug_print = True
    else:
        debug_print = False

    if (debug_print):
        print("Line {}: {}".format(count, s.strip()))

        if s == "\n":
            print("===================")

    ###
    ##if count > 1000:
    ##    break

###
file_dict.close()
