from os import mkdir
from os.path import exists

import requests
from bs4 import BeautifulSoup


# from https://sozdik.soyle.kz/glossary


#
def clean_word(word_kz):
    word_kz = word_kz.replace("</span>", "").replace("<br/>", "").replace("</td>", "").replace("<td>", "")
    word_kz = word_kz.replace("<td width=\"250\">", "").replace("(", "").replace(")", "")
    word_kz = word_kz.replace("\"", "")
    word_kz = word_kz.strip().lower()
    return word_kz


#
url_mp3 = "https://sozdik.soyle.kz/"

#
dir_base = "./"

#
path_mp3 = dir_base + "mp3_grab/"
if not exists(path_mp3):
    mkdir(path_mp3)
#
path_tmp = dir_base + "tmp/"
if not exists(path_tmp):
    mkdir(path_tmp)

#
file = open(dir_base + "content.html", "r")
file_text = file.read()
file.close()

# В csv - файл
csv_file_name = "./dat.csv"
file_csv = open(csv_file_name, 'wt', encoding="utf-8")
file_csv.write("kaz\trus\tcategory1\n")

#
soup_category = BeautifulSoup(file_text, 'html.parser')

#
n = 0
for element_category in soup_category.find_all('li'):
    n = n + 1
    text = str(element_category)
    # print(text)

    #
    key0 = "data-cid=\""
    key0_1 = "\" href="
    key1 = "<span class=\"s1\">"
    key1_1 = "</span>"
    key2 = "<span class=\"s2\">"
    key2_1 = "</span>"
    #
    n0 = text.find(key0)
    n0_1 = text.find(key0_1, n0)
    n1 = text.find(key1)
    n1_1 = text.find(key1_1, n1)
    n2 = text.find(key2)
    n2_1 = text.find(key2_1, n2)

    # if n2 == -1:
    #    continue

    # Вырезаем
    str_category_id = text[n0 + len(key0):n0_1]
    str_category = text[n1 + len(key1):n1_1]
    str_count_words = text[n2 + len(key2):n2_1]

    #
    if n2 == -1 and str_category_id == "95":
        str_count_words = "250"

    #
    print("")
    print(str_category + " [" + str(n) + "]")

    # Запрос на список слов
    url_list = "https://sozdik.soyle.kz/glossary/list"
    url_list_1 = "?cid=" + str_category_id

    #
    page = 1
    done_words = 0
    count_words = int(str_count_words)
    while done_words < count_words:
        print("")
        print(str_category + " [" + str(n) + "], page [" + str(page) + "]")

        #
        r_words_page = requests.get(url_list + url_list_1, allow_redirects=True)
        open("./tmp/_cid-" + str_category_id + "-" + str(page) + ".html", 'wb').write(r_words_page.content)

        #
        soup_words = BeautifulSoup(r_words_page.content, 'html.parser')

        m = 0
        for element_word in soup_words.find_all('label'):
            m = m + 1

            text = str(element_word)
            # print(text)

            #
            key0 = "onclick=\"play('"
            key0_1 = "', '"
            key1 = "<span class=\"name\">"
            key1_1 = "<span>"
            key2 = "<span>–"
            key2_1 = "</span>"
            #
            n0 = text.find(key0)
            n0_1 = text.find(key0_1, n0)
            n1 = text.find(key1)
            n1_1 = text.find(key1_1, n1)
            n2 = text.find(key2)
            n2_1 = text.find(key2_1, n2)

            # Вырезаем
            str_mp3 = text[n0 + len(key0):n0_1]
            word_kz = text[n1 + len(key1):n1_1]
            word_ru = text[n2 + len(key2):n2_1]

            #
            word_ru = clean_word(word_ru)
            word_kz = clean_word(word_kz)
            if len(word_ru) == 0 or len(word_kz) == 0 or len(str_mp3) == 0:
                raise Exception("len==0")

            #
            print(word_kz)

            #
            file_csv.write(word_kz + "\t" + word_ru + "\t" + str_category + "\n")
            file_csv.flush()

            # Читаем mp3
            mp3_file_name = path_mp3 + word_kz + '.mp3'

            # пропустим ранее прочитанное
            if exists(mp3_file_name):
                continue

            # читаем mp3
            try:
                r_words_page = requests.get(url_mp3 + str_mp3, allow_redirects=True)
                open(mp3_file_name, 'wb').write(r_words_page.content)
            except Exception as e:
                print(e)

        #
        done_words = done_words + 20
        page = page + 1

        #
        url_list_1 = "/page/" + str(page) + "?cid=" + str_category_id

#
file_csv.close()
