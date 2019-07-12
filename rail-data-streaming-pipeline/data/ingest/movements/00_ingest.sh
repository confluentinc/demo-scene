#!/usr/bin/env bash

curl -i -X POST -H "Accept:application/json" \
-H  "Content-Type:application/json" http://localhost:8083/connectors/ \
-d '{
  "name": "source-activemq-networkrail-TRAIN_MVT_EA_TOC-01",
  "config": {
    "connector.class": "io.confluent.connect.activemq.ActiveMQSourceConnector",
    "activemq.url": "tcp://datafeeds.networkrail.co.uk:61619",
    "activemq.username": "${file:/data/credentials.properties:NROD_USERNAME}",
    "activemq.password": "${file:/data/credentials.properties:NROD_PASSWORD}",
    "jms.destination.type": "topic",
    "jms.destination.name": "TRAIN_MVT_EA_TOC",
    "kafka.topic": "networkrail_TRAIN_MVT",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "false",
    "confluent.license": "",
    "confluent.topic.bootstrap.servers": "kafka:29092",
    "confluent.topic.replication.factor": 1
  }
}'


curl -i -X POST -H "Accept:application/json" \
-H  "Content-Type:application/json" http://localhost:8083/connectors/ \
-d '{
  "name": "source-activemq-networkrail-TRAIN_MVT_HB_TOC-01",
  "config": {
    "connector.class": "io.confluent.connect.activemq.ActiveMQSourceConnector",
    "activemq.url": "tcp://datafeeds.networkrail.co.uk:61619",
    "activemq.username": "${file:/data/credentials.properties:NROD_USERNAME}",
    "activemq.password": "${file:/data/credentials.properties:NROD_PASSWORD}",
    "jms.destination.type": "topic",
    "jms.destination.name": "TRAIN_MVT_HB_TOC",
    "kafka.topic": "networkrail_TRAIN_MVT",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "false",
    "confluent.license": "",
    "confluent.topic.bootstrap.servers": "kafka:29092",
    "confluent.topic.replication.factor": 1
  }
}'
curl -i -X POST -H "Accept:application/json" \
-H  "Content-Type:application/json" http://localhost:8083/connectors/ \
-d '{
  "name": "source-activemq-networkrail-TRAIN_MVT_EM_TOC-01",
  "config": {
    "connector.class": "io.confluent.connect.activemq.ActiveMQSourceConnector",
    "activemq.url": "tcp://datafeeds.networkrail.co.uk:61619",
    "activemq.username": "${file:/data/credentials.properties:NROD_USERNAME}",
    "activemq.password": "${file:/data/credentials.properties:NROD_PASSWORD}",
    "jms.destination.type": "topic",
    "jms.destination.name": "TRAIN_MVT_EM_TOC",
    "kafka.topic": "networkrail_TRAIN_MVT",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "false",
    "confluent.license": "",
    "confluent.topic.bootstrap.servers": "kafka:29092",
    "confluent.topic.replication.factor": 1
  }
}'
curl -i -X POST -H "Accept:application/json" \
-H  "Content-Type:application/json" http://localhost:8083/connectors/ \
-d '{
  "name": "source-activemq-networkrail-TRAIN_MVT_ED_TOC-01",
  "config": {
    "connector.class": "io.confluent.connect.activemq.ActiveMQSourceConnector",
    "activemq.url": "tcp://datafeeds.networkrail.co.uk:61619",
    "activemq.username": "${file:/data/credentials.properties:NROD_USERNAME}",
    "activemq.password": "${file:/data/credentials.properties:NROD_PASSWORD}",
    "jms.destination.type": "topic",
    "jms.destination.name": "TRAIN_MVT_ED_TOC",
    "kafka.topic": "networkrail_TRAIN_MVT",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "false",
    "confluent.license": "",
    "confluent.topic.bootstrap.servers": "kafka:29092",
    "confluent.topic.replication.factor": 1
  }
}'
