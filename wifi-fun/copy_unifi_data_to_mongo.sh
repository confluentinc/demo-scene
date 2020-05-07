#!/bin/bash
#
# Ref: https://rmoff.net/2019/12/17/copy-mongodb-collections-from-remote-to-local-instance/

for c in device user
do
    ssh robin@unifi \
        mongodump --port 27117 --db=ace --collection=$c --out=- | \
    docker exec --interactive mongodb \
        mongorestore --dir=- --db=ace --collection=$c
done