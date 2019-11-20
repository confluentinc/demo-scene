#!/bin/bash 

docker exec kafka-1 /usr/bin/kafka-topics --zookeeper zookeeper:2181 --create --topic data_mqtt --replication-factor 3 --partitions 6

kafkacat -b localhost:9092 -t data_mqtt -K: -P -T -l ./data/dummy_data.kcat
