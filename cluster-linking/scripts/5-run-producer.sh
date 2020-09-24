#!/bin/bash

echo -e "\n\n==> Produce: West -> East west-trades \n"

docker-compose exec broker-west bash -c 'seq 1 1000 | kafka-console-producer \
    --broker-list broker-west:19091 \
    --topic west-trades'