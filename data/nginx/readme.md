### Установка nginx

Установка:

```
sudo apt update
sudo apt install nginx
```

Проверить работу службы:

```
systemctl status nginx
```

Остановить, запустить остановленную, перезапустить службу:

```
systemctl stop nginx
systemctl start nginx
systemctl restart nginx
```

Перезагружаться без отключения соединений (после внесения изменения в конфигурацию):

```
systemctl reload nginx
```

### Настройка

Обычно конфигурация лежит в `/etc/nginx/nginx.conf`

Краткая памятка по конфигурации: `https://nginx.org/ru/docs/beginners_guide.html`

### Настройка SSL под nginx

1.

В nginx настроить ssl и проксирование с 443 на 8080, см. `nginx.conf`

```
http {
    
    ...
    
    server {
        listen 443 ssl;
        ssl_certificate /home/nginx/ssl/wordstrike_me.crt;
        ssl_certificate_key /home/nginx/ssl/private_key;
        
        location / {
            proxy_pass http://localhost:8080;
        }
    }

}
```

2.

Приложение запускать на порту 8080:

```
./app-run.sh serve -p 8080 -c /
```
