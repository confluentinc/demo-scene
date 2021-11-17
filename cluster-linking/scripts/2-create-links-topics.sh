#!/bin/bash

echo -e "\n==> Create West Demo Topic"
docker-compose exec broker-west kafka-topics  --create \
	--bootstrap-server broker-west:19091 \
	--topic west-trades \
	--partitions 1 \
	--replication-factor 1 \
	--config min.insync.replicas=1

sleep 2

echo -e "\n==> Create East -> West link"
docker-compose exec broker-east bash -c 'echo "{\"groupFilters\": [{\"name\": \"*\",\"patternType\": \"LITERAL\",\"filterType\": \"INCLUDE\"}]}" > groupFilters.json'
docker-compose exec broker-east kafka-cluster-links \
	--bootstrap-server broker-east:19092 \
	--create \
	--link west-cluster-link \
	--config bootstrap.servers=broker-west:19091,consumer.offset.sync.enable=true,consumer.offset.sync.ms=10000 \
	--consumer-group-filters-json-file groupFilters.json

sleep 2

echo -e "\n==> Create an east mirror of west-trades"

docker-compose exec broker-east kafka-mirrors --create \
	--bootstrap-server broker-east:19092 \
	--mirror-topic west-trades \
	--link west-cluster-link
