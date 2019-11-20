#!/usr/bin/env bash

curl -X PUT -H  "Content-Type:application/json" http://localhost:28083/connectors/sink-elastic-train_movements_01-v01/config \
    -d '{
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "topics": "TRAIN_MOVEMENTS_01",
    "connection.url": "http://elasticsearch:9200",
    "type.name": "type.name=kafkaconnect",
    "key.ignore": "false",
    "schema.ignore": "true",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter"
}'

curl -X PUT -H  "Content-Type:application/json" http://localhost:28083/connectors/sink-elastic-train_movements_activations_schedule_00-v01/config \
    -d '{
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "topics": "TRAIN_MOVEMENTS_ACTIVATIONS_SCHEDULE_00",
    "connection.url": "http://elasticsearch:9200",
    "type.name": "type.name=kafkaconnect",
    "key.ignore": "false",
    "schema.ignore": "true",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter"
}'

curl -X PUT -H  "Content-Type:application/json" http://localhost:28083/connectors/sink-elastic-train_cancellations_activations_schedule_00-v01/config \
    -d '{
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "topics": "TRAIN_CANCELLATIONS_ACTIVATIONS_SCHEDULE_00",
    "connection.url": "http://elasticsearch:9200",
    "type.name": "type.name=kafkaconnect",
    "key.ignore": "false",
    "schema.ignore": "true",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter"
}'

curl -X PUT -H  "Content-Type:application/json" http://localhost:28083/connectors/sink-elastic-train_cancellations_02-v01/config \
    -d '{
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "topics": "TRAIN_CANCELLATIONS_02",
    "connection.url": "http://elasticsearch:9200",
    "type.name": "type.name=kafkaconnect",
    "key.ignore": "false",
    "schema.ignore": "true",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter"
}'

curl -X PUT -H  "Content-Type:application/json" http://localhost:28083/connectors/sink-elastic-schedule_02-v01/config \
    -d '{
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "topics": "SCHEDULE_02",
    "connection.url": "http://elasticsearch:9200",
    "type.name": "type.name=kafkaconnect",
    "key.ignore": "false",
    "schema.ignore": "true",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter"
}'

curl -X PUT -H  "Content-Type:application/json" http://localhost:28083/connectors/sink-elastic-stanox-v01/config \
    -d '{
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "topics": "STANOX_FLAT_KEYED",
    "connection.url": "http://elasticsearch:9200",
    "type.name": "type.name=kafkaconnect",
    "key.ignore": "false",
    "schema.ignore": "true",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter"
}'

# curl -X POST -H "Accept:application/json" \
#     -H  "Content-Type:application/json" http://localhost:28083/connectors/ \
#     -d '{
#   "name": "sink-elastic-schedule_01-v01",
#   "config": {
#     "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
#     "topics": "SCHEDULE_01",
#     "connection.url": "http://elasticsearch:9200",
#     "type.name": "type.name=kafkaconnect",
#     "key.ignore": "true",
#     "schema.ignore": "true",
#     "key.converter": "org.apache.kafka.connect.storage.StringConverter"
#   }
# }'


#curl -X POST -H "Accept:application/json" \
#    -H  "Content-Type:application/json" http://localhost:28083/connectors/ \
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

