#!/bin/sh

curl -i -X POST -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:8083/connectors/ \
    -d '{
      "name": "mysql-source-demo-customers",
      "config": {
            "connector.class": "io.debezium.connector.mysql.MySqlConnector",
            "database.hostname": "localhost",
            "database.port": "3306",
            "database.user": "debezium",
            "database.password": "dbz",
            "database.server.id": "42",
            "database.server.name": "asgard",
            "table.whitelist": "demo.customers",
            "database.history.kafka.bootstrap.servers": "localhost:9092",
            "database.history.kafka.topic": "dbhistory.demo" ,
            "include.schema.changes": "true",
            "transforms": "unwrap,InsertTopic,InsertSourceDetails",
            "transforms.unwrap.type": "io.debezium.transforms.UnwrapFromEnvelope",
            "transforms.InsertTopic.type":"org.apache.kafka.connect.transforms.InsertField$Value",
            "transforms.InsertTopic.topic.field":"messagetopic",
            "transforms.InsertSourceDetails.type":"org.apache.kafka.connect.transforms.InsertField$Value",
            "transforms.InsertSourceDetails.static.field":"messagesource",
            "transforms.InsertSourceDetails.static.value":"Debezium CDC from MySQL on asgard"
       }
    }'
curl -i -X POST -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:8083/connectors/ \
    -d '{
      "name": "mysql-source-demo-customers-raw",
      "config": {
            "connector.class": "io.debezium.connector.mysql.MySqlConnector",
            "database.hostname": "localhost",
            "database.port": "3306",
            "database.user": "debezium",
            "database.password": "dbz",
            "database.server.id": "43",
            "database.server.name": "asgard",
            "table.whitelist": "demo.customers",
            "database.history.kafka.bootstrap.servers": "localhost:9092",
            "database.history.kafka.topic": "dbhistory.demo" ,
            "include.schema.changes": "true",
            "transforms": "addTopicSuffix",
            "transforms.addTopicSuffix.type":"org.apache.kafka.connect.transforms.RegexRouter",
            "transforms.addTopicSuffix.regex":"(.*)",
            "transforms.addTopicSuffix.replacement":"$1-raw"       }
    }'
curl -X "POST" "http://localhost:8083/connectors/" \
     -H "Content-Type: application/json" \
     -d '{
  "name": "es_sink_RATINGS_ENRICHED",
  "config": {
    "topics": "'RATINGS_ENRICHED'",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "key.ignore": "true",
    "schema.ignore": "false",
    "type.name": "type.name=kafkaconnect",
    "topic.index.map": "'RATINGS_ENRICHED':'ratings_enriched'",
    "connection.url": "http://localhost:9200",
    "transforms": "ExtractTimestamp",
    "transforms.ExtractTimestamp.type": "org.apache.kafka.connect.transforms.InsertField$Value",
    "transforms.ExtractTimestamp.timestamp.field" : "EXTRACT_TS"
  }
}'

