#!/usr/bin/env bash

curl -i -X PUT -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:8083/connectors/sink-neo4j-train-cancellations-v05/config \
    -d '{
    "connector.class": "streams.kafka.connect.sink.Neo4jSinkConnector",
    "topics": "TRAIN_CANCELLATIONS",
    "key.converter":"org.apache.kafka.connect.storage.StringConverter",
    "errors.tolerance": "none",
    "errors.deadletterqueue.topic.name": "sink-neo4j-train-v05_dlq",
    "errors.deadletterqueue.topic.replication.factor": 1,
    "errors.deadletterqueue.context.headers.enable":true,
    "kafka.bootstrap.servers":"broker:29092",
    "neo4j.server.uri": "bolt://neo4j:7687",
    "neo4j.authentication.basic.username": "neo4j",
    "neo4j.authentication.basic.password": "connect",
    "neo4j.topic.cypher.TRAIN_CANCELLATIONS": "MERGE (train:train{id: coalesce(event.TRAIN_ID,'\''<unknown>'\''), category: coalesce(event.TRAIN_CATEGORY,'\''<unknown>'\''), power: coalesce(event.POWER_TYPE,'\''<unknown>'\'')}) MERGE (toc:toc{toc: coalesce(event.TOC,'\''<unknown>'\'')}) MERGE (canx_reason:canx_reason{reason: coalesce(event.CANX_REASON,'\''<unknown>'\'')}) MERGE (canx_loc:canx_loc{location: coalesce(event.CANCELLATION_LOCATION,'\''<unknown>'\'')}) MERGE (train)-[:OPERATED_BY]->(toc) MERGE (canx_loc)<-[:CANCELLED_AT{reason:event.CANX_REASON, time:event.CANX_TIMESTAMP}]-(train)-[:CANCELLED_BECAUSE]->(canx_reason)"
       } '

