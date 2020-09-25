#!/bin/bash

echo -e "\n\n==> Consume from east cluster, west-trades \n"

docker-compose exec broker-east kafka-console-consumer \
    --bootstrap-server broker-east:19092 \
    --topic west-trades \
    --from-beginning
