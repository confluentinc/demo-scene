#!/bin/bash

echo -e "\n==> Stop migrating the consumer group someGroup via the west link"
docker-compose exec broker-east bash -c 'echo "consumer.offset.group.filters={\"groupFilters\": [{\"name\": \"*\",\"patternType\": \"LITERAL\",\"filterType\": \"INCLUDE\"},{\"name\":\"someGroup\",\"patternType\":\"LITERAL\",\"filterType\":\"EXCLUDE\"}]}" > newGroupFilters.properties'
docker-compose exec broker-east kafka-configs \
    --bootstrap-server broker-east:19092 \
    --alter \
    --cluster-link west-cluster-link \
    --add-config-file newGroupFilters.properties

sleep 2

echo -e "\n==> Produce 100 more messages to the source topic"
docker-compose exec broker-west bash -c 'seq 101 200 | kafka-console-producer \
    --broker-list broker-west:19091 \
    --topic west-trades'

docker-compose exec broker-east bash -c 'echo enable.auto.commit=true > consumer.properties'
docker-compose exec broker-east bash -c 'echo group.id=someGroup >> consumer.properties'

echo -e "\n\n==> Consume from east cluster, west-trades and commit offsets (destination cluster)\n"
docker-compose exec broker-east kafka-console-consumer \
    --bootstrap-server broker-east:19092 \
    --topic west-trades \
    --consumer.config consumer.properties \
    --timeout-ms 10000

echo -e "\n\n==> Notice that that consumer started at 101 because it already had consumed messages 1 to 100 on the west cluster.\n"

echo -e "\n\n==> Monitor that the consumer offsets have correctly been migrated \n"
echo -e "\n\n==> West Cluster \n"
docker-compose exec broker-west kafka-consumer-groups  \
    --bootstrap-server broker-west:19091 \
    --describe \
    --group someGroup

echo -e "\n\n==> East Cluster \n"
docker-compose exec broker-east kafka-consumer-groups  \
    --bootstrap-server broker-east:19092 \
    --describe \
    --group someGroup