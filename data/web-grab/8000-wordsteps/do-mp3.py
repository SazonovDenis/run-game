import requests
from os.path import exists
from os import mkdir

#
url_base = ''

#
dir_base = "./"

#
mp3_file_dir = dir_base + "mp3/"
if not exists(mp3_file_dir):
    mkdir(mp3_file_dir)

#
f = open(dir_base + '1.html', 'r')

#
n = 0
while True:
    line = f.readline()
    if not line:
        break

    #
    pos_from = line.find("http://wordsteps.com/")
    pos_to = line.find(".mp3")

    if pos_from == -1 or pos_to == -1:
        continue

    #
    n = n + 1

    #
    url = line[pos_from:pos_to + 4]
    urls = url.split("/")
    word = urls[len(urls) - 1]

    #
    mp3_file_name = mp3_file_dir + word

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
    print(word + ", " + str(n))

#
f.close()
