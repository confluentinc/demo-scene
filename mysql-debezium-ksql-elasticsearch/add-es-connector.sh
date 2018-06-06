#!/bin/sh

curl -X "POST" "http://localhost:18083/connectors/" \
     -H "Content-Type: application/json" \
     -d '{
  "name": "es_sink_RATINGS_ENRICHED",
  "config": {
    "topics": "'RATINGS_ENRICHED'",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "key.ignore": "true",
    "schema.ignore": "true",
    "type.name": "type.name=kafkaconnect",
    "topic.index.map": "'RATINGS_ENRICHED':'ratings_enriched'",
    "connection.url": "http://elasticsearch:9200",
    "transforms": "ExtractTimestamp",
    "transforms.ExtractTimestamp.type": "org.apache.kafka.connect.transforms.InsertField$Value",
    "transforms.ExtractTimestamp.timestamp.field" : "EXTRACT_TS"
  }
}'

