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


### Установка как сервис


1.
Разместить файл `/data/product/wordstrike.me.service` в `/etc/systemd/system/`


2.
Проверить, что файл `app-run-serve.sh` расположен там, где указано в файле `wordstrike.me.service`, в секции `Service`, параметр `ExecStart`:

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

