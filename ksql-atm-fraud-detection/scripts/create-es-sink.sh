#!/bin/sh

# ---- Sink to Elasticsearch using timestamp-based index
#
# To use this, the source topic needs to already be in lowercase
#
# In KSQL you can do this with WITH (KAFKA_TOPIC='my-lowercase-topic') 
# when creating a stream or table
#
curl -s \
     -X "POST" "http://localhost:18083/connectors/" \
     -H "Content-Type: application/json" \
     -d '{
  "name": "es_sink_atm_txns",
  "config": {
    "topics": "atm_txns",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": false,
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "key.ignore": "true",
    "schema.ignore": "true",
    "type.name": "type.name=kafkaconnect",
    "connection.url": "http://elasticsearch:9200",
    "transforms": "routeTS",
    "transforms.routeTS.type":"org.apache.kafka.connect.transforms.TimestampRouter",  
    "transforms.routeTS.topic.format":"kafka-${topic}-${timestamp}",  
    "transforms.routeTS.timestamp.format":"YYYY-MM"
  }
}'


# ---- Sink to Elasticsearch with uppercase topic
#
# Use topic.index.map to map uppercase topic to lower case index name
#
# Note that this is not currently compatible with TimestampRouter
#
curl -X "POST" "http://kafka-connect:18083/connectors/" \
     -H "Content-Type: application/json" \
     -d '{
  "name": "es_sink_ATM_POSSIBLE_FRAUD2",
  "config": {
    "topics": "ATM_POSSIBLE_FRAUD2",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": false,
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "key.ignore": "true",
    "schema.ignore": "true",
    "type.name": "type.name=kafkaconnect",
    "topic.index.map": "ATM_POSSIBLE_FRAUD2:atm_possible_fraud",
    "connection.url": "http://elasticsearch:9200"
  }
}'
