#!/usr/bin/env bash

curl -X PUT -H  "Content-Type:application/json" http://localhost:8083/connectors/sink_elastic_v01/config \
    -d '{
    "connector.class"                : "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "topics"                         : "TRAIN_MOVEMENTS, TRAIN_CANCELLATIONS",
    "connection.url"                 : "http://elasticsearch:9200",
    "type.name"                      : "_doc",
    "key.ignore"                     : "false",
    "schema.ignore"                  : "true",
    "behavior.on.malformed.documents": "ignore",
    "tasks.max"                      : 4
}'

curl -X PUT -H  "Content-Type:application/json" http://localhost:8083/connectors/sink-elastic-stanox-and-schedule-v01/config \
    -d '{
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "topics"         : "SCHEDULE,LOCATIONS_BY_STANOX",
    "connection.url" : "http://elasticsearch:9200",
    "type.name"      : "_doc",
    "key.ignore"     : "false",
    "schema.ignore"  : "true"
    }'
