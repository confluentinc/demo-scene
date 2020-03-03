#!/bin/bash
#
# Ref: https://docs.ksqldb.io/en/latest/developer-guide/ksqldb-rest-api/ksql-endpoint/
#
# This works but is really horrid to look at, because the whole of the `ksql` value has to be a single line. 
curl -X "POST" "http://ksqldb:8088/ksql" \
     -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
     -d '{"ksql":"CREATE STREAM RATINGS WITH (KAFKA_TOPIC='\''ratings'\'',VALUE_FORMAT='\''AVRO'\''); CREATE STREAM POOR_RATINGS AS SELECT STARS, CHANNEL, MESSAGE FROM RATINGS WHERE STARS<3;"}'

# Splitting each command into a separate call makes more sense: 
curl -X "POST" "http://ksqldb:8088/ksql" \
     -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
     -d '{"ksql":"CREATE STREAM RATINGS2 WITH (KAFKA_TOPIC='\''ratings'\'',VALUE_FORMAT='\''AVRO'\'');"}'     
curl -X "POST" "http://ksqldb:8088/ksql" \
     -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
     -d '{"ksql":"CREATE STREAM POOR_RATINGS2 AS SELECT STARS, CHANNEL, MESSAGE FROM RATINGS WHERE STARS<3;"}'
