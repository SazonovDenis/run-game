import requests

#
url_base = 'https://britlex.ru/mp3/'

#
dir_base = "./"
f = open(dir_base + '1.en.csv', 'r')

#
n = 0
while True:
    line = f.readline()
    if not line:
        break

    #
    en = line.strip()
    url = url_base + en + ".mp3"
    r = requests.get(url, allow_redirects=True)
    open(dir_base + "mp3/" + en + '.mp3', 'wb').write(r.content)

    #
    n = n + 1
    print(en + ", " + str(n))

#
f.close()
