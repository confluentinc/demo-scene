#!/usr/bin/env bash
# ---- Sink to Elasticsearch a test topic
#
#
curl -X "POST" "http://kafka-connect-cp:18083/connectors/" \
     -H "Content-Type: application/json" \
     -d '{
    "name": "es_sink_jfokus19",
    "config": {
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": false,
    "topics": "jfokus19",
    "key.ignore": "true",
    "schema.ignore": "true",
    "type.name": "type.name=kafkaconnect",
    "connection.url": "http://elasticsearch:9200"
  }
}'

