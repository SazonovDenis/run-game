import requests
from os.path import exists
from os import mkdir
from bs4 import BeautifulSoup

# from https://kreekly.com

#
dir_base = "./"

#
path = dir_base + "mp3/"
if not exists(path):
    mkdir(path)

#
url = 'https://kreekly.com/lists/10000-samyh-populyarnyh-angliyskih-slov/'
r = requests.get(url, allow_redirects=True)
open("./page.html", 'wb').write(r.content)

print("url: " + url + " - ok")

#
file_csv = open("./dat.csv", 'wt')
file_csv.write("eng\trus\ttrans\n")

#
soup = BeautifulSoup(r.text, 'html.parser')

#
n = 0
for element in soup.find_all('div', class_='dict-word'):
    n = n + 1

    #
    mp3_el = element.find_all('div', class_="word-btns")
    mp3_lst = mp3_el[0].contents[1]["data-sound"].split(",")
    word_eng = element.find_all('span', class_="eng")[0].text
    word_rus = element.find_all('span', class_="rus")[0].text
    word_trans = element.find_all('span', class_="no-mobile transcript")[0].text

    #
    for mp3 in mp3_lst:
        url = "https://kreekly.com/mp3/" + mp3
        mp3_split = mp3.split("-")
        dict_name = mp3_split[len(mp3_split) - 1].split(".")[0]
        mp3_file_dir = dir_base + "mp3/" + dict_name + "/"
        mp3_file_name = mp3_file_dir + word_eng + '.mp3'

        if not exists(mp3_file_dir):
            mkdir(mp3_file_dir)

        # пропустим ранее прочитанное
        if exists(mp3_file_name):
            continue

        # читаем mp3
        try:
            r = requests.get(url, allow_redirects=True)
            open(mp3_file_name, 'wb').write(r.content)
        except Exception as e:
            print(e)

    #
    file_csv.write(word_eng + "\t" + word_rus + "\t" + word_trans + "\n")
    file_csv.flush()

    #
    print(word_eng + ": " + word_rus + ", " + str(n))

#
file_csv.close()
