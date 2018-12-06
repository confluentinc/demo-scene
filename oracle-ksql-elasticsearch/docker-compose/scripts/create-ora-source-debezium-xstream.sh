#!/bin/sh

curl -i -X POST -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://connect-debezium:8083/connectors/ \
    -d '{
      "name": "ora-source-debezium-xstream",
      "config": {
            "connector.class": "io.debezium.connector.oracle.OracleConnector",
            "database.server.name" : "asgard",
            "database.hostname" : "oracle",
            "database.port" : "1521",
            "database.user" : "c##xstrm",
            "database.password" : "xs",
            "database.dbname" : "ORCLCDB",
            "database.pdb.name" : "ORCLPDB1",
            "database.out.server.name" : "dbzxout_new",
            "database.history.kafka.bootstrap.servers" : "kafka:29092",
            "database.history.kafka.topic": "schema-changes.inventory",
            "include.schema.changes": "true",
            "table.blacklist":"ORCLPDB1.AUDSYS.*",
            "key.converter": "io.confluent.connect.avro.AvroConverter",
            "key.converter.schema.registry.url": "http://schema-registry:8081",
            "value.converter": "io.confluent.connect.avro.AvroConverter",
            "value.converter.schema.registry.url": "http://schema-registry:8081",
            "transforms": "InsertTopic,InsertSourceDetails",
            "transforms.InsertTopic.type":"org.apache.kafka.connect.transforms.InsertField$Value",
            "transforms.InsertTopic.topic.field":"messagetopic",
            "transforms.InsertSourceDetails.type":"org.apache.kafka.connect.transforms.InsertField$Value",
            "transforms.InsertSourceDetails.static.field":"messagesource",
            "transforms.InsertSourceDetails.static.value":"Debezium CDC from Oracle on asgard"
       }
    }'

            # ,
            # "transforms": "addTopicSuffix",
            # "transforms.addTopicSuffix.type":"org.apache.kafka.connect.transforms.RegexRouter",
            # "transforms.addTopicSuffix.regex":"(.*)",
            # "transforms.addTopicSuffix.replacement":"$1-raw"

#             ,
#             "transforms": "unwrap,InsertTopic,InsertSourceDetails",
#             "transforms.unwrap.type": "io.debezium.transforms.UnwrapFromEnvelope",
#             "transforms.InsertTopic.type":"org.apache.kafka.connect.transforms.InsertField$Value",
#             "transforms.InsertTopic.topic.field":"messagetopic",
#             "transforms.InsertSourceDetails.type":"org.apache.kafka.connect.transforms.InsertField$Value",
#             "transforms.InsertSourceDetails.static.field":"messagesource",
#             "transforms.InsertSourceDetails.static.value":"Debezium CDC from Oracle on asgard"
