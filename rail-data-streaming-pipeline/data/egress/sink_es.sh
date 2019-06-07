#!/usr/bin/env bash

curl -XDELETE localhost:9200/_template/kafkaconnect

curl -XPUT "http://localhost:9200/_template/kafkaconnect/" -H 'Content-Type: application/json' -d' {
  "index_patterns": "*",
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  },
  "mappings": {
    "_default_": {
      "dynamic_templates": [
        {
          "dates01": {
            "match": "*_TIMESTAMP",
            "mapping": {
              "type": "date",
              "ignore_malformed": true
            }
          }
        },
        {
          "non_analysed_string_template": {
            "match": "*",
            "match_mapping_type": "string",
            "mapping": {
              "type": "keyword"
            }
          }
        }
      ]
    }
  }
}'


curl -i -X POST -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:8083/connectors/ \
    -d '{
  "name": "sink-elastic-train-cancellations-v03",
  "config": {
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "topics": "TRAIN_CANCELLATIONS_02",
    "connection.url": "http://elasticsearch:9200",
    "type.name": "type.name=kafkaconnect",
    "key.ignore": "true",
    "schema.ignore": "true",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter"
  }
}'


curl -i -X POST -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:8083/connectors/ \
    -d '{
  "name": "sink-elastic-train-movements-v05",
  "config": {
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "topics": "TRAIN_MOVEMENTS_ACTIVATIONS_SCHEDULE_00",
    "connection.url": "http://elasticsearch:9200",
    "type.name": "type.name=kafkaconnect",
    "key.ignore": "false",
    "schema.ignore": "true",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "transforms": "changeTopic",
    "transforms.changeTopic.type":"org.apache.kafka.connect.transforms.RegexRouter",
    "transforms.changeTopic.regex":"TRAIN_MOVEMENTS_ACTIVATIONS_SCHEDULE_00",
    "transforms.changeTopic.replacement":"train-movements"
  }
}'

#curl -i -X POST -H "Accept:application/json" \
#    -H  "Content-Type:application/json" http://localhost:8083/connectors/ \
#    -d '{
#  "name": "sink-elastic-train-movements-v03",
#  "config": {
#    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
#    "topics": "TRAIN_MOVEMENTS_ACTIVATIONS_SCHEDULE_00",
#    "connection.url": "http://elasticsearch:9200",
#    "type.name": "type.name=kafkaconnect",
#    "key.ignore": "false",
#    "schema.ignore": "true",
#    "transforms": "ValueToKey,extractKey,changeTopic",
#    "transforms.ValueToKey.type":"org.apache.kafka.connect.transforms.ValueToKey",
#    "transforms.ValueToKey.fields":"MSG_KEY",
#    "transforms.extractKey.type":"org.apache.kafka.connect.transforms.ExtractField$Key",
#    "transforms.extractKey.field":"MSG_KEY",    
#    "transforms.changeTopic.type":"org.apache.kafka.connect.transforms.RegexRouter",
#    "transforms.changeTopic.regex":"TRAIN_MOVEMENTS_ACTIVATIONS_SCHEDULE_00",
#    "transforms.changeTopic.replacement":"train-movements",
#    "key.converter": "org.apache.kafka.connect.storage.StringConverter"
#  }
#}'

curl -i -X POST -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:8083/connectors/ \
    -d '{
  "name": "sink-elastic-schedule-v01",
  "config": {
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "topics": "SCHEDULE_01",
    "connection.url": "http://elasticsearch:9200",
    "type.name": "type.name=kafkaconnect",
    "key.ignore": "true",
    "schema.ignore": "true",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter"
  }
}'

curl -i -X POST -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:8083/connectors/ \
    -d '{
  "name": "sink-elastic-train-movements-basic-v01",
  "config": {
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "topics": "TRAIN_MOVEMENTS_01",
    "connection.url": "http://elasticsearch:9200",
    "type.name": "type.name=kafkaconnect",
    "key.ignore": "true",
    "schema.ignore": "true",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter"
  }
}'
