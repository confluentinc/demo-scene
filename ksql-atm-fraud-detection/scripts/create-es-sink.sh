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
  "name": "sink_elastic_atm_txns",
  "config": {
    "topics": "atm_txns_gess",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": false,
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "key.ignore": "true",
    "schema.ignore": "true",
    "type.name": "kafkaconnect",
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
  "name": "sink_elastic_ATM_POSSIBLE_FRAUD",
  "config": {
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "connection.url": "http://elasticsearch:9200",
    "type.name": "kafkaconnect",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "key.ignore": "true",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": false,
    "schema.ignore": "true",
    "topics": "ATM_POSSIBLE_FRAUD",
    "topic.index.map": "ATM_POSSIBLE_FRAUD:atm_possible_fraud"
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
  "name": "sink_elastic_ATM_POSSIBLE_FRAUD_ENRICHED",
  "config": {
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "connection.url": "http://elasticsearch:9200",
    "type.name": "kafkaconnect",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "key.ignore": "true",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": false,
    "schema.ignore": "true",
    "topics": "ATM_POSSIBLE_FRAUD_ENRICHED",
    "topic.index.map": "ATM_POSSIBLE_FRAUD_ENRICHED:atm_possible_fraud_enriched"
  }
}'
