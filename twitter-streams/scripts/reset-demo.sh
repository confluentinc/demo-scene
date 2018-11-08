#!/bin/bash

docker-compose down
docker volume ls -q --filter dangling=true | xargs docker volume rm
rm -rf data/