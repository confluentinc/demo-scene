#!/usr/bin/env bash

curl -i -X PUT -H "Accept:application/json" \
-H  "Content-Type:application/json" http://localhost:8083/connectors/source-activemq-networkrail-TRAIN_MVT_EA_TOC-01/config \
-d '{
    "connector.class"                    : "io.confluent.connect.activemq.ActiveMQSourceConnector",
    "activemq.url"                       : "tcp://datafeeds.networkrail.co.uk:61619",
    "activemq.username"                  : "${file:/data/credentials.properties:NROD_USERNAME}",
    "activemq.password"                  : "${file:/data/credentials.properties:NROD_PASSWORD}",
    "jms.destination.type"               : "topic",
    "jms.destination.name"               : "TRAIN_MVT_EA_TOC",
    "kafka.topic"                        : "networkrail_train_mvt",
    "value.converter"                    : "io.confluent.connect.avro.AvroConverter",
    "value.converter.schema.registry.url": "http://schema-registry:8081",
    "key.converter"                      : "io.confluent.connect.avro.AvroConverter",
    "key.converter.schema.registry.url"  : "http://schema-registry:8081",
    "confluent.license"                  : "",
    "confluent.topic.bootstrap.servers"  : "broker:29092",
    "confluent.topic.replication.factor" : 1
}'

curl -i -X PUT -H "Accept:application/json" \
-H  "Content-Type:application/json" http://localhost:8083/connectors/source-activemq-networkrail-TRAIN_MVT_HB_TOC-01/config \
-d '{
    "connector.class"                    : "io.confluent.connect.activemq.ActiveMQSourceConnector",
    "activemq.url"                       : "tcp://datafeeds.networkrail.co.uk:61619",
    "activemq.username"                  : "${file:/data/credentials.properties:NROD_USERNAME}",
    "activemq.password"                  : "${file:/data/credentials.properties:NROD_PASSWORD}",
    "jms.destination.type"               : "topic",
    "jms.destination.name"               : "TRAIN_MVT_HB_TOC",
    "kafka.topic"                        : "networkrail_train_mvt",
    "value.converter"                    : "io.confluent.connect.avro.AvroConverter",
    "value.converter.schema.registry.url": "http://schema-registry:8081",
    "key.converter"                      : "io.confluent.connect.avro.AvroConverter",
    "key.converter.schema.registry.url"  : "http://schema-registry:8081",
    "confluent.license"                  : "",
    "confluent.topic.bootstrap.servers"  : "broker:29092",
    "confluent.topic.replication.factor" : 1
}'
curl -i -X PUT -H "Accept:application/json" \
-H  "Content-Type:application/json" http://localhost:8083/connectors/source-activemq-networkrail-TRAIN_MVT_EM_TOC-01/config \
-d '{
    "connector.class"                    : "io.confluent.connect.activemq.ActiveMQSourceConnector",
    "activemq.url"                       : "tcp://datafeeds.networkrail.co.uk:61619",
    "activemq.username"                  : "${file:/data/credentials.properties:NROD_USERNAME}",
    "activemq.password"                  : "${file:/data/credentials.properties:NROD_PASSWORD}",
    "jms.destination.type"               : "topic",
    "jms.destination.name"               : "TRAIN_MVT_EM_TOC",
    "kafka.topic"                        : "networkrail_train_mvt",
    "value.converter"                    : "io.confluent.connect.avro.AvroConverter",
    "value.converter.schema.registry.url": "http://schema-registry:8081",
    "key.converter"                      : "io.confluent.connect.avro.AvroConverter",
    "key.converter.schema.registry.url"  : "http://schema-registry:8081",
    "confluent.license"                  : "",
    "confluent.topic.bootstrap.servers"  : "broker:29092",
    "confluent.topic.replication.factor" : 1
}'

curl -i -X PUT -H "Accept:application/json" \
-H  "Content-Type:application/json" http://localhost:8083/connectors/source-activemq-networkrail-TRAIN_MVT_ED_TOC-01/config \
-d '{
    "connector.class"                    : "io.confluent.connect.activemq.ActiveMQSourceConnector",
    "activemq.url"                       : "tcp://datafeeds.networkrail.co.uk:61619",
    "activemq.username"                  : "${file:/data/credentials.properties:NROD_USERNAME}",
    "activemq.password"                  : "${file:/data/credentials.properties:NROD_PASSWORD}",
    "jms.destination.type"               : "topic",
    "jms.destination.name"               : "TRAIN_MVT_ED_TOC",
    "kafka.topic"                        : "networkrail_train_mvt",
    "value.converter"                    : "io.confluent.connect.avro.AvroConverter",
    "value.converter.schema.registry.url": "http://schema-registry:8081",
    "key.converter"                      : "io.confluent.connect.avro.AvroConverter",
    "key.converter.schema.registry.url"  : "http://schema-registry:8081",
    "confluent.license"                  : "",
    "confluent.topic.bootstrap.servers"  : "broker:29092",
    "confluent.topic.replication.factor" : 1
}'
