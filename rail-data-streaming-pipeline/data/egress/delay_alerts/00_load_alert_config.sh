#!/usr/bin/env bash

docker exec kafka /usr/bin/kafka-topics --zookeeper zookeeper:2181 --create --topic alert_config --replication-factor 1 --partitions 1 --config cleanup.policy=compact 

kafkacat -b localhost:9092 -t alert_config -P -K: <<EOF
BEN RHYDDING:{"STATION":"BEN RHYDDING","ALERT_OVER_MINS":"5"}
LEEDS:{"STATION":"LEEDS","ALERT_OVER_MINS":"45"}
KINGS CROSS:{"STATION":"KINGS CROSS","ALERT_OVER_MINS":"30"}
EOF
