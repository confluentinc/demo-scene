#!/usr/bin/env bash

curl -X PUT -H  "Content-Type:application/json" http://localhost:8083/connectors/sink_elastic_train_movements_and_cancellations-v01/config \
    -d '{
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "topics"         : "TRAIN_MOVEMENTS, TRAIN_CANCELLATIONS",
    "connection.url" : "http://elasticsearch:9200",
    "type.name"      : "_doc",
    "key.ignore"     : "false",
    "schema.ignore"  : "true",
    "key.converter"  : "org.apache.kafka.connect.storage.StringConverter"
}'

# Single Message Transform is used here to prepend the created index with `train-` which is 
# necessary for the template to match for dynamic mappings
curl -X PUT -H  "Content-Type:application/json" http://localhost:8083/connectors/sink-elastic-stanox-and-schedule-v01/config \
    -d '{
    "connector.class"                : "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "topics"                         : "SCHEDULE_00,STANOX_FLAT_KEYED",
    "connection.url"                 : "http://elasticsearch:9200",
    "type.name"                      : "_doc",
    "key.ignore"                     : "false",
    "schema.ignore"                  : "true",
    "key.converter"                  : "org.apache.kafka.connect.storage.StringConverter",
    "transforms"                     : "renameIX",
    "transforms.renameIX.type"       : "org.apache.kafka.connect.transforms.RegexRouter",
    "transforms.renameIX.regex"      : "(.*)",
    "transforms.renameIX.replacement": "train-$1"
}'


# ----------
# Deprecated
# ----------
# curl -X PUT -H  "Content-Type:application/json" http://localhost:8083/connectors/sink-elastic-train_movements_01-v01/config \
#     -d '{
#     "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
#     "topics": "TRAIN_MOVEMENTS_00",
#     "connection.url": "http://elasticsearch:9200",
#     "type.name": "_doc",
#     "key.ignore": "false",
#     "schema.ignore": "true",
#     "key.converter": "org.apache.kafka.connect.storage.StringConverter"
# }'
# curl -X PUT -H  "Content-Type:application/json" http://localhost:8083/connectors/sink-elastic-train_movements_activations_schedule_00-v01/config \
#     -d '{
#     "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
#     "topics"         : "TRAIN_MOVEMENTS_ACTIVATIONS_SCHEDULE_00",
#     "connection.url" : "http://elasticsearch:9200",
#     "type.name"      : "_doc",
#     "key.ignore"     : "false",
#     "schema.ignore"  : "true",
#     "key.converter"  : "org.apache.kafka.connect.storage.StringConverter"
# }'

# curl -X PUT -H  "Content-Type:application/json" http://localhost:8083/connectors/sink-elastic-train_cancellations_activations_schedule_00-v01/config \
#     -d '{
#     "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
#     "topics"         : "TRAIN_CANCELLATIONS_ACTIVATIONS_SCHEDULE_00",
#     "connection.url" : "http://elasticsearch:9200",
#     "type.name"      : "_doc",
#     "key.ignore"     : "false",
#     "schema.ignore"  : "true",
#     "key.converter"  : "org.apache.kafka.connect.storage.StringConverter"
# }'

# curl -X PUT -H  "Content-Type:application/json" http://localhost:8083/connectors/sink-elastic-train_cancellations_02-v01/config \
#     -d '{
#     "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
#     "topics"         : "TRAIN_CANCELLATIONS_00",
#     "connection.url" : "http://elasticsearch:9200",
#     "type.name"      : "_doc",
#     "key.ignore"     : "false",
#     "schema.ignore"  : "true",
#     "key.converter"  : "org.apache.kafka.connect.storage.StringConverter"
# }'
