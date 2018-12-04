#!/bin/sh

docker-compose exec oracle bash -c '/opt/oracle/scripts/startup/01_create_customers.sh'
docker-compose exec oracle bash -c '/opt/oracle/scripts/startup/02_populate_customers.sh'
docker-compose stop kafka
docker-compose rm -f kafka
docker-compose up -d kafka
