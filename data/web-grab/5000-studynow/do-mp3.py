import requests
from os.path import exists
from os import mkdir

#
url_base = 'https://britlex.ru/mp3/'

#
dir_base = "./"

#
mp3_file_dir = dir_base + "mp3/"
if not exists(mp3_file_dir):
    mkdir(mp3_file_dir)


f = open(dir_base + '_en.csv', 'r')

#
n = 0
while True:
    n = n + 1

    #
    line = f.readline()
    if not line:
        break

    # пропустим ранее прочитанное
    #if (n<3870):
    #    continue

    #
    word = line.strip()
    url = url_base + word.split("(")[0].strip() + ".mp3"
    mp3_file_name = mp3_file_dir + word + '.mp3'


    # пропустим ранее прочитанное
    if exists(mp3_file_name):
        continue

    #
    r = requests.get(url, allow_redirects=True)
    open(mp3_file_name, 'wb').write(r.content)

    #
    print(word + ", " + str(n))

#
f.close()
