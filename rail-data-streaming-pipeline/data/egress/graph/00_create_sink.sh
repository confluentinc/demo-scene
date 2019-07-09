#!/usr/bin/env bash

curl -i -X PUT -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:28083/connectors/sink-neo4j-train-cancellations/config \
    -d '{
    "connector.class": "streams.kafka.connect.sink.Neo4jSinkConnector",
    "topics": "TRAIN_CANCELLATIONS_02",
    "key.converter":"org.apache.kafka.connect.storage.StringConverter",
    "errors.tolerance": "all",
    "errors.deadletterqueue.topic.name": "sink-neo4j-train-00_dlq",
    "errors.deadletterqueue.topic.replication.factor": 1,
    "errors.deadletterqueue.context.headers.enable":true,
    "neo4j.server.uri": "bolt://neo4j:7687",
    "neo4j.authentication.basic.username": "neo4j",
    "neo4j.authentication.basic.password": "connect",
    "neo4j.topic.cypher.TRAIN_CANCELLATIONS_02": "MERGE (train:train{id: event.TRAIN_ID}) MERGE (toc:toc{toc: event.TOC}) MERGE (canx_reason:canx_reason{reason: event.CANX_REASON}) MERGE (canx_loc:canx_loc{location: coalesce(event.CANCELLATION_LOCATION,\'<unknown>\')}) MERGE (train)-[:OPERATED_BY]->(toc) MERGE (canx_loc)<-[:CANCELLED_AT{reason:event.CANX_REASON, time:event.CANX_TIMESTAMP}]-(train)-[:CANCELLED_BECAUSE]->(canx_reason)"
       } '

