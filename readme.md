## Создание заполненной БД

### Создание пустой БД

``` shell
jc db-create
```

### Скачиваем аудиофайлы

1.

Проверить, что установлен `python3` и `pip`

```
sudo apt install python3
sudo apt install python3-pip
```

2.

Проверить, что установлены библиотеки

```
pip install requests
pip install beautifulsoup4
```

3.

Выполнить скачиване

```
cd data/web-grab
./grab-mp3.sh
```

В папках `data/web-grab/***/mp3` появятся скачанные mp3-файлы для каждого английского
слова.

Для казахских слов (и прочих файлов, содержищих киррилицу) выполняется коприрование
каждого файла из папки `data/web-grab/kz.sozdik.soyle.kz/mp3_grab`
в папку `data/web-grab/kz.sozdik.soyle.kz/mp3` с переименованием:
`килилическое имя.mp3` -> `md5 hash кириллического имени.mp3`

```
cd data/web-grab/kz.sozdik.soyle.kz
./do-encode-mp3.py
```

Когда надо будет найти слово `word`, функция getSoundFiles сначала поищет файл `word.mp3`,
а потом его md5-hash, обрезанный до 16 символов, например: `c47d187067c6cf95.mp3`.

### Наполняем базу словами, уровнями и заданиями

```shell
./db-create.sh 
```

Сработает соответствующая фикстура.

## Установка как сервис

1.

Разместить файл `/data/product/wordstrike.me.service` в `/etc/systemd/system/`

2.

Проверить, что файл `app-run-serve.sh` расположен там, где указано в
файле `wordstrike.me.service`, в секции `Service`, параметр `ExecStart`:

```
[Service]
ExecStart=/home/wordstrike.me/product/app-run-serve.sh
```

3.

Установить как сервис

```
systemctl daemon-reload

systemctl enable wordstrike.me
```

4.

Запустить и проверить статус

```
systemctl start wordstrike.me

systemctl status wordstrike.me
```

### Установка tesseract

1.

Установка

```
sudo apt-get install tesseract-ocr
```

2.

Скачиваем языки (`https://github.com/tesseract-ocr/tessdata`). Файлы `eng.traineddata`
и `rus.traineddata` разместить в `/usr/local/share/tessdata`

3.

Проверяем

```
tesseract /home/dvsa/1.jpeg /home/dvsa/t2.txt -l eng --tessdata-dir /usr/local/share/tessdata/
```

### Настройка nginx

1.

Чтобы можно было отправлять большие запросы нужно в nginx настроить размер запроса,
см. `nginx.conf`

```
http {
    
    ...
    
    client_max_body_size 20M;

    ...
}
```

### Настройка postgres

Чтобы делался бэкап с командной строки - разрешить подключение с паролем для юзера
postgres. В файле `/etc/postgresql/14/main/pg_hba.conf` строку:

```
# Database administrative login by Unix domain socket
local   all             postgres                                peer
```

... заменить на:

```
# Database administrative login by Unix domain socket
local   all             postgres                                md5
```

После чего перезапустить postgres

```
sudo systemctl restart postgresql
```
