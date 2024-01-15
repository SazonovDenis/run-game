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
