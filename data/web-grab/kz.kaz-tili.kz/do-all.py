from os import mkdir
from os.path import exists

from bs4 import BeautifulSoup


# from https://kaz-tili.kz/slovar01.htm

#
def clean_word(word_kz):
    word_kz = word_kz.replace("</span>", "").replace("<br/>", "").replace("</td>", "").replace("<td>", "")
    word_kz = word_kz.replace("<td width=\"250\">", "").replace("(", "").replace(")", "")
    word_kz = word_kz.replace("\"", "")
    word_kz = word_kz.strip().lower()
    return word_kz


#
dir_base = "./"

#
html_list = [
    "01.html",
    "02.html",
    "03.html",
    "04.html",
]

#
n = 0
for html in html_list:
    #
    n = n + 1
    file_csv = open("./dat" + str(n) + ".csv", 'wt', encoding="utf-8")
    file_csv.write("kaz\trus\tcategory1\n")

    #
    file = open(dir_base + html, "r")
    file_text = file.read()
    file.close()

    #
    soup = BeautifulSoup(file_text, 'html.parser')

    #
    state = 0
    for element in soup.find_all('tr'):
        text = str(element)

        if state == 0:
            if text.find("<b>Автор: Татьяна Валяева</b>") != -1:
                state = 1

            continue

        if state == 1:
            if text.find("<tr bgcolor=") == 0:
                state = 2

        if state == 2:
            if text.find("наиболее употребительных") != -1:
                state = 3

        #
        if state != 2:
            continue

        #
        # print(text)

        #
        key0 = "<tr bgcolor=\"#"
        key1 = "<span class=\"kaz\">"
        key2 = "<span class=\"vi\">"
        key3 = "<span class=\"se\">"
        key4 = "</span></td></tr>"
        #
        n0 = text.find(key0)
        n1 = text.find(key1)
        n2 = text.find(key2)
        n3 = text.find(key3)
        n4 = text.find(key4)
        if n2 == -1:
            n2 = n3
        #
        if n0 != 0:
            continue

        # Вырезаем
        word_kz = text[n1 + len(key1):n2]
        word_kz1 = text[n2 + len(key2):n3]
        word_rus = text[n3 + len(key3):n4]
        tag = text[n0 + len(key0):n0 + 6 + len(key0)]

        # Чистим 1
        n33 = word_kz.find("</span")
        if n33 != -1:
            word_kz = word_kz[0:n33]
        n44 = word_rus.find("</span")
        if n44 != -1:
            word_rus = word_rus[0:n44]
        # Чистим 2
        word_kz = clean_word(word_kz)
        word_kz1 = clean_word(word_kz1)
        word_rus = clean_word(word_rus)
        tag = clean_word(tag)

        #
        if tag == "fdeced":
            tag = "top-100"
        elif tag == "fff4fd":
            tag = "top-200"
        elif tag == "fffff2":
            tag = "top-500"
        elif tag == "white":
            tag = "top-1000"
        else:
            raise Exception("bad tag: " + tag)

        #
        file_csv.write(word_kz + "\t" + word_rus + "\t" + tag + "\n")
        file_csv.flush()

    #
    file_csv.close()
