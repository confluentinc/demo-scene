#!/bin/bash

echo -e "\n\n==> Consume from east cluster, west-trades. \n Consumer will timeout after 15 seconds of not receiving any new messages. \n"

docker-compose exec broker-east kafka-console-consumer \
    --bootstrap-server broker-east:19092 \
    --topic west-trades \
    --from-beginning \
    --timeout-ms 15000
