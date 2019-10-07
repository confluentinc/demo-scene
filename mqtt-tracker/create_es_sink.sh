#!/bin/bash

source .env

curl -i -X PUT -H  "Content-Type:application/json" \
      http://localhost:8083/connectors/sink-elastic-runner_location-00/config \
      -d '{
            "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
            "connection.url": "'$ELASTIC_URL'",
            "connection.username": "'$ELASTIC_USERNAME'",
            "connection.password": "'$ELASTIC_PASSWORD'",
            "type.name": "",
            "behavior.on.malformed.documents": "warn",
            "errors.tolerance": "all",
            "errors.log.enable":true,
            "errors.log.include.messages":true,
            "topics.regex": "RUNNER_LOCATION_.*",
            "key.ignore": "true",
            "schema.ignore": "true",
            "key.converter": "org.apache.kafka.connect.storage.StringConverter"
            }'

