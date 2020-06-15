#!/bin/bash

echo -e $(date) "Firing up Oracle"
docker-compose up -d oracle

# ---- Wait for Oracle DB to be up (takes several minutes to instantiate) ---
echo -e "\n--\n\n$(date) Waiting for Oracle to be available … ⏳"
grep -q "DATABASE IS READY TO USE!" <(docker logs -f oracle)
echo -e "$(date) Installing rlwrap on Oracle container"
docker exec -it -u root oracle bash -c "rpm -Uvh https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm &&  yum install -y rlwrap"
# docker exec --interactive --tty --user root --workdir / $(docker ps --filter "name=oracle" --quiet) bash -c 'rpm -Uvh https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm'
# docker exec --interactive --tty --user root --workdir / $(docker ps --filter "name=oracle" --quiet) bash -c 'yum install -y rlwrap'

# echo -e $(date) "Firing up the rest of the stack"
# docker-compose up -d

# # ---- Set up connectors ---
# echo -e "\n\n=============\nWaiting for Kafka Connect to start listening on localhost ⏳\n=============\n"
# while [ $(curl -s -o /dev/null -w %{http_code} http://localhost:8083/connectors) -ne 200 ] ; do
#   echo -e "\t" $(date) " Kafka Connect listener HTTP state: " $(curl -s -o /dev/null -w %{http_code} http://localhost:8083/connectors) " (waiting for 200)"
#   sleep 5
# done
# echo -e $(date) "\n\n--------------\n\o/ Kafka Connect is ready! Listener HTTP state: " $(curl -s -o /dev/null -w %{http_code} http://localhost:8083/connectors) "\n--------------\n"

# echo -e "\n--\n$(date) +> Creating Kafka Connect Oracle source (XStreams)"
# curl -i -X PUT -H "Accept:application/json" \
#     -H  "Content-Type:application/json" http://localhost:8083/connectors/ora-source-debezium-xstream/config \
#     -d '{
#             "connector.class": "io.debezium.connector.oracle.OracleConnector",
#             "database.server.name" : "asgard",
#             "database.hostname" : "oracle",
#             "database.port" : "1521",
#             "database.user" : "c##xstrm",
#             "database.password" : "xs",
#             "database.dbname" : "ORCLCDB",
#             "database.pdb.name" : "ORCLPDB1",
#             "database.out.server.name" : "dbzxout",
#             "database.history.kafka.bootstrap.servers" : "kafka:29092",
#             "database.history.kafka.topic": "schema-changes.inventory",
#             "include.schema.changes": "true",
#             "table.blacklist":"ORCLPDB1.AUDSYS.*",
#             "key.converter": "io.confluent.connect.avro.AvroConverter",
#             "key.converter.schema.registry.url": "http://schema-registry:8081",
#             "value.converter": "io.confluent.connect.avro.AvroConverter",
#             "value.converter.schema.registry.url": "http://schema-registry:8081",
#             "transforms": "InsertTopic,InsertSourceDetails",
#             "transforms.InsertTopic.type":"org.apache.kafka.connect.transforms.InsertField$Value",
#             "transforms.InsertTopic.topic.field":"messagetopic",
#             "transforms.InsertSourceDetails.type":"org.apache.kafka.connect.transforms.InsertField$Value",
#             "transforms.InsertSourceDetails.static.field":"messagesource",
#             "transforms.InsertSourceDetails.static.value":"Debezium CDC from Oracle on asgard"
#     }'        

# echo -e "\n--\n$(date) +> Creating Kafka Connect Oracle source (JDBC)"
# curl -i -X PUT -H "Accept:application/json" \
#     -H  "Content-Type:application/json" http://localhost:8083/connectors/ora-source-jdbc/config/ \
#     -d '{
#             "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
#             "connection.url": "jdbc:oracle:thin:@oracle:1521/ORCLPDB1",
#             "connection.user":"Debezium",
#             "connection.password":"dbz",
#             "numeric.mapping":"best_fit",
#             "mode":"timestamp",
#             "poll.interval.ms":"1000",
#             "validate.non.null":"false",
#             "table.whitelist":"CUSTOMERS",
#             "timestamp.column.name":"UPDATE_TS",
#             "topic.prefix":"ora-",
#             "transforms": "addTopicSuffix,InsertTopic,InsertSourceDetails",
#             "transforms.InsertTopic.type":"org.apache.kafka.connect.transforms.InsertField$Value",
#             "transforms.InsertTopic.topic.field":"messagetopic",
#             "transforms.InsertSourceDetails.type":"org.apache.kafka.connect.transforms.InsertField$Value",
#             "transforms.InsertSourceDetails.static.field":"messagesource",
#             "transforms.InsertSourceDetails.static.value":"JDBC Source Connector from Oracle on asgard",
#             "transforms.addTopicSuffix.type":"org.apache.kafka.connect.transforms.RegexRouter",
#             "transforms.addTopicSuffix.regex":"(.*)",
#             "transforms.addTopicSuffix.replacement":"$1-jdbc"
#         }'
# #
    
