#!/bin/bash

source .env

curl -i -X PUT -H  "Content-Type:application/json" \
      http://localhost:8083/connectors/sink-elastic-runner_status-00/config \
      -d '{
            "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
            "connection.url": "'$ELASTIC_URL'",
            "connection.username": "'$ELASTIC_USERNAME'",
            "connection.password": "'$ELASTIC_PASSWORD'",
            "type.name": "type.name=kafkaconnect",
            "behavior.on.malformed.documents": "warn",
            "topics": "RUNNER_STATUS",
            "key.ignore": "false",
            "schema.ignore": "true",
            "key.converter": "org.apache.kafka.connect.storage.StringConverter",
            "transforms": "addTS",
            "transforms.addTS.type": "org.apache.kafka.connect.transforms.InsertField$Value",
            "transforms.addTS.timestamp.field": "EVENT_TS"
            }'

curl -i -X PUT -H  "Content-Type:application/json" \
      http://localhost:8083/connectors/sink-elastic-runner_location-00/config \
      -d '{
            "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
            "connection.url": "'$ELASTIC_URL'",
            "connection.username": "'$ELASTIC_USERNAME'",
            "connection.password": "'$ELASTIC_PASSWORD'",
            "type.name": "type.name=kafkaconnect",
            "behavior.on.malformed.documents": "warn",
            "topics": "RUNNER_LOCATION",
            "key.ignore": "true",
            "schema.ignore": "true",
            "key.converter": "org.apache.kafka.connect.storage.StringConverter",
            "transforms": "addTS",
            "transforms.addTS.type": "org.apache.kafka.connect.transforms.InsertField$Value",
            "transforms.addTS.timestamp.field": "EVENT_TS"
            }'