#!/bin/sh

curl -i -X POST -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://connect-debezium:8083/connectors/ \
    -d '{
      "name": "ora-source-demo-customers",
      "config": {
            "connector.class": "io.debezium.connector.oracle.OracleConnector",
            "tasks.max" : "1",
            "database.server.name" : "asgard",
            "database.hostname" : "oracle",
            "database.port" : "1521",
            "database.user" : "c##xstrm",
            "database.password" : "xsaFOO",
            "database.dbname" : "ORCLCDB",
            "database.pdb.name" : "ORCLPDB1",
            "database.out.server.name" : "dbzxout",
            "database.history.kafka.bootstrap.servers" : "kafka:29092",
            "database.history.kafka.topic": "schema-changes.inventory",
            "table.whitelist":"CUSTOMERS",
            "include.schema.changes": "true",
            "transforms": "unwrap,InsertTopic,InsertSourceDetails",
            "transforms.unwrap.type": "io.debezium.transforms.UnwrapFromEnvelope",
            "transforms.InsertTopic.type":"org.apache.kafka.connect.transforms.InsertField$Value",
            "transforms.InsertTopic.topic.field":"messagetopic",
            "transforms.InsertSourceDetails.type":"org.apache.kafka.connect.transforms.InsertField$Value",
            "transforms.InsertSourceDetails.static.field":"messagesource",
            "transforms.InsertSourceDetails.static.value":"Debezium CDC from Oracle on asgard"
       }
    }'

# curl -i -X POST -H "Accept:application/json" \
#     -H  "Content-Type:application/json" http://connect-debezium:8083/connectors/ \
#     -d '{
#       "name": "ora-source-demo-customers-raw",
#       "config": {
#             "connector.class": "io.debezium.connector.oracle.OracleConnector",
#             "tasks.max" : "1",
#             "database.server.name" : "asgard",
#             "database.hostname" : "oracle",
#             "database.port" : "1521",
#             "database.user" : "c##xstrm",
#             "database.password" : "xsa",
#             "database.dbname" : "ORCLCDB",
#             "database.pdb.name" : "ORCLPDB1",
#             "database.out.server.name" : "dbzxout",
#             "database.history.kafka.bootstrap.servers" : "kafka:29092",
#             "database.history.kafka.topic": "schema-changes.inventory",
#             "include.schema.changes": "true",
#             "transforms": "addTopicSuffix",
#             "transforms.addTopicSuffix.type":"org.apache.kafka.connect.transforms.RegexRouter",
#             "transforms.addTopicSuffix.regex":"(.*)",
#             "transforms.addTopicSuffix.replacement":"$1-raw"       
#             }       
#     }'

