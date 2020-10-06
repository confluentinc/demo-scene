#! /bin/bash

 echo -e "\n\n==> Creating a demo topic \n"

docker exec broker kafka-topics \
    --bootstrap-server localhost:9092 \
    --create \
    --topic demo-topic \
    --partitions 1

echo -e "\n\n==> Producing to demo-topic \n"

docker exec broker kafka-producer-perf-test --topic demo-topic \
    --num-records 5000000 \
    --record-size 5000 \
    --throughput -1 \
    --producer-props \
        acks=all \
        bootstrap.servers=localhost:9092 \
        batch.size=8196