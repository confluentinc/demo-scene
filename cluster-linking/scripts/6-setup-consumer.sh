#!/bin/bash

docker-compose exec broker-west bash -c 'echo enable.auto.commit=true > consumer.properties'
docker-compose exec broker-west bash -c 'echo group.id=someGroup >> consumer.properties'

echo -e "\n\n==> Consume from west cluster, west-trades and commit offsets (source cluster) \n"
docker-compose exec broker-west kafka-console-consumer \
    --bootstrap-server broker-west:19091 \
    --topic west-trades \
    --from-beginning \
    --consumer.config consumer.properties \
    --timeout-ms 10000