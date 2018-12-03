#!/bin/sh
curl -i -X POST -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://kafka-connect-cp:18083/connectors/ \
    -d '{
      "name": "ora-source-jdbc",
      "config": {
            "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
            "connection.url": "jdbc:oracle:thin:@oracle:1521/ORCLPDB1",
            "connection.user":"Debezium",
            "connection.password":"dbz",
            "numeric.mapping":"best_fit",
            "mode":"incrementing",
            "incrementing.column.name":"ID",
            "transforms": "addTopicSuffix,InsertTopic,InsertSourceDetails",
            "transforms.InsertTopic.type":"org.apache.kafka.connect.transforms.InsertField$Value",
            "transforms.InsertTopic.topic.field":"messagetopic",
            "transforms.InsertSourceDetails.type":"org.apache.kafka.connect.transforms.InsertField$Value",
            "transforms.InsertSourceDetails.static.field":"messagesource",
            "transforms.InsertSourceDetails.static.value":"Debezium CDC from Oracle on asgard"
            "transforms.addTopicSuffix.type":"org.apache.kafka.connect.transforms.RegexRouter",
            "transforms.addTopicSuffix.regex":"(.*)",
            "transforms.addTopicSuffix.replacement":"$1-jdbc"
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
