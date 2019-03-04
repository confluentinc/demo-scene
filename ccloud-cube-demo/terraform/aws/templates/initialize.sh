#!/bin/bash

curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" -d '{ "schema": "[{ \"type\": \"record\", \"name\": \"event\", \"fields\": [ { \"name\": \"name\", \"type\": \"string\" }, { \"name\": \"motion\", \"type\": { \"name\": \"motion\", \"type\": \"record\", \"fields\":[ { \"name\":\"x\", \"type\":\"int\" }, { \"name\":\"y\", \"type\":\"int\" }, { \"name\":\"z\", \"type\":\"int\" } ] } }, { \"name\": \"speed\", \"type\": { \"name\": \"speed\", \"type\": \"record\", \"fields\":[ { \"name\":\"x\", \"type\":\"int\" }, { \"name\":\"y\", \"type\":\"int\" }, { \"name\":\"z\", \"type\":\"int\" } ] } } ] } ]"}' ${schema_registry_url}/subjects/_EVENTS-value/versions

curl -X POST -H "Content-Type: application/vnd.kafka.avro.v2+json" -d '{"value_schema_id" : 1, "records" : [{"value" : { "event" : { "name" : "ccloud-demo", "motion" : { "x" : 0, "y" : 0, "z" : 0 }, "speed" : { "x" : 0, "y" : 0, "z" : 0 }}}}]}' ${rest_proxy_url}/topics/_EVENTS

ksql ${ksql_server_url} <<EOF

  CREATE TABLE NUMBERS (NUMBER BIGINT, X INTEGER, Y INTEGER, Z INTEGER) WITH (KAFKA_TOPIC='_NUMBERS', VALUE_FORMAT='JSON', KEY='NUMBER');
  CREATE STREAM EVENTS WITH (KAFKA_TOPIC='_EVENTS', VALUE_FORMAT='AVRO');
  CREATE STREAM EVENTS_ENRICHED AS SELECT NAME, MOTION->X AS X, MOTION->Y AS Y, MOTION->Z AS Z, 3 AS NUMBER FROM EVENTS;
  CREATE STREAM WINNERS AS SELECT E.NAME AS NAME FROM EVENTS_ENRICHED E LEFT OUTER JOIN NUMBERS N ON E.NUMBER = N.NUMBER WHERE E.X = N.X AND E.Y = N.Y AND E.Z = N.Z;
  CREATE TABLE WINNERS_AUDIT AS SELECT E.NAME AS NAME, COUNT(*) AS TOTAL FROM EVENTS_ENRICHED E LEFT OUTER JOIN NUMBERS N ON E.NUMBER = N.NUMBER WHERE E.X = N.X AND E.Y = N.Y AND E.Z = N.Z GROUP BY NAME;
  
EOF