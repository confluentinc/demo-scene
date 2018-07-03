#!/bin/sh

curl -X "POST" "http://localhost:18083/connectors/" \
     -H "Content-Type: application/json" \
     -d '{
             "name": "jdbc_sink_postgres",
             "config": {
                 "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
                 "connection.url": "jdbc:postgresql://postgres:5432/database?user=postgres&password=postgres",
                 "auto.create":"true",
                 "topics":"ratings, UNHAPPY_PLATINUM_CUSTOMERS"
             }
     }'
