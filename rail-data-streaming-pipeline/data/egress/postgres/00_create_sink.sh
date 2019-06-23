#!/usr/bin/env bash

curl -X DELETE http://localhost:28083/connectors/sink-jdbc-TRAIN_MOVEMENTS_ACTIVATIONS_SCHEDULE_00

curl -X POST http://localhost:28083/connectors -H "Content-Type: application/json" -d '{
  "name": "sink-jdbc-TRAIN_MOVEMENTS_ACTIVATIONS_SCHEDULE_00",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "connection.url": "jdbc:postgresql://postgres:5432/",
    "connection.user": "postgres",
    "connection.password": "postgres",
    "auto.create": true,
    "auto.evolve": true,
    "insert.mode": "upsert",
    "pk.mode": "record_key",
    "pk.fields": "MESSAGE_KEY",
    "topics": "TRAIN_MOVEMENTS_ACTIVATIONS_SCHEDULE_00",
	"transforms": "dropArrays",
        "transforms.dropArrays.type": "org.apache.kafka.connect.transforms.ReplaceField$Value",
        "transforms.dropArrays.blacklist": "SCHEDULE_SEGMENT_LOCATION, HEADER"
  }
}'

