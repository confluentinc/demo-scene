#!/bin/bash

for foo in alarm device event rogue user
do
    ssh robin@unifi mongodump --port 27117 --db=ace --collection=$foo --out=- | docker exec --interactive syslog_mongodb mongorestore --dir=- --db=ace --collection=$foo
done

