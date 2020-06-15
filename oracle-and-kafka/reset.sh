#!/bin/sh

docker-compose exec oracle bash -c '/opt/oracle/scripts/startup/01_create_customers.sh'
docker-compose exec oracle bash -c '/opt/oracle/scripts/startup/02_populate_customers.sh'
docker-compose stop kafka zookeeper postgres
docker-compose rm -f kafka zookeeper postgres
docker ps -a
docker-compose up -d kafka postgres
docker-compose restart kafka-connect schema-registry ksqldb