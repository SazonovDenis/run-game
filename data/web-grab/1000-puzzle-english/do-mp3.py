import requests
from os.path import exists
from os import mkdir

#
url_base = 'https://images.puzzle-english.com/words/'
dir_base = "./"
dicts = [
    'collins_US',  # data-accent="us"
    'vocalware_hugh_UK',  # data-accent="uk"
    'campbridge_UK',  # data-accent="uk"
    'SteveElliott_UK',  # data-accent="uk"
    'campbridge_US',  # data-accent="us"
    'collins_UK',  # data-accent="uk"
    'peterBaker_UK',  # data-accent="uk"
    'macmillan_UK',  # data-accent="uk"
    'freedictionary_UK',  # data-accent="uk"
    'howjsay_UK',  # data-accent="uk"
    'yandex_UK',  # data-accent="uk"
    'freedictionary_US',  # data-accent="us"
]

for dict_name in dicts:
    path = dir_base + "mp3/" + dict_name
    if not exists(path):
        mkdir(path)

#
f = open(dir_base + '_en.csv', 'r')

#
n = 0
while True:
    n = n + 1

    #
    line = f.readline()
    if not line:
        break

    #
    word = line.strip()
    for dict_name in dicts:
        url = url_base + dict_name + "/" + word + ".mp3"
        mp3_file_name = dir_base + "mp3/" + dict_name + "/" + word + '.mp3'

        # пропустим ранее прочитанное
        if exists(mp3_file_name):
            continue

        #
        try:
            r = requests.get(url, allow_redirects=True)
            open(mp3_file_name, 'wb').write(r.content)
        except Exception as e:
            print(e)

    #
    print(word + ", " + str(n))

#
f.close()
