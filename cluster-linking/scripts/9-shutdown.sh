#!/bin/bash

docker container stop pumba-latency
docker-compose down -v --remove-orphans
sleep 1
