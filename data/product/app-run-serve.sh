#! /bin/bash

mkdir -p /home/wordstrike.me/log

date +"%Y-%m-%d %T" >> /home/wordstrike.me/log/log.txt


cd /home/wordstrike.me/product/ 

/home/wordstrike.me/product/app-run.sh serve -log -p 8080 -c / &
