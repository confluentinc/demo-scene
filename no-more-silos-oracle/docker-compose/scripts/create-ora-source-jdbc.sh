#!/bin/sh
# https://docs.confluent.io/current/connect/kafka-connect-jdbc/source-connector/source_config_options.html
curl -i -X POST -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:18083/connectors/ \
    -d '{
      "name": "ora-source-jdbc",
      "config": {
            "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
            "connection.url": "jdbc:oracle:thin:@oracle:1521/ORCLPDB1",
            "connection.user":"Debezium",
            "connection.password":"dbz",
            "numeric.mapping":"best_fit",
            "mode":"timestamp",
            "poll.interval.ms":"1000",
            "validate.non.null":"false",
            "table.whitelist":"CUSTOMERS",
            "timestamp.column.name":"UPDATE_TS",
            "topic.prefix":"ora-",
            "transforms": "addTopicSuffix,InsertTopic,InsertSourceDetails",
            "transforms.InsertTopic.type":"org.apache.kafka.connect.transforms.InsertField$Value",
            "transforms.InsertTopic.topic.field":"messagetopic",
            "transforms.InsertSourceDetails.type":"org.apache.kafka.connect.transforms.InsertField$Value",
            "transforms.InsertSourceDetails.static.field":"messagesource",
            "transforms.InsertSourceDetails.static.value":"JDBC Source Connector from Oracle on asgard",
            "transforms.addTopicSuffix.type":"org.apache.kafka.connect.transforms.RegexRouter",
            "transforms.addTopicSuffix.regex":"(.*)",
            "transforms.addTopicSuffix.replacement":"$1-jdbc"
       }
    }'

            # "mode":"incrementing",
            # "incrementing.column.name":"ID",

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
