#!/bin/bash
export CONNECT_REST_ADVERTISED_HOST_NAME=localhost
export CONNECT_REST_PORT=8083
echo "Waiting for Kafka Connect to start listening on $CONNECT_REST_ADVERTISED_HOST_NAME ⏳"
while [ $(curl -s -o /dev/null -w %{http_code} http://$CONNECT_REST_ADVERTISED_HOST_NAME:$CONNECT_REST_PORT/connectors) -eq 000 ] ; do 
  echo -e $(date) " Kafka Connect listener HTTP state: " $(curl -s -o /dev/null -w %{http_code} http://$CONNECT_REST_ADVERTISED_HOST_NAME:$CONNECT_REST_PORT/connectors) " (waiting for 200)"
  sleep 5 
done

curl -X POST -H "Content-Type: application/json" --data @config/source-data-users-avro.json http://localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" --data @config/source-data-users-json-noschema.json http://localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" --data @config/source-data-users-json-withschema.json http://localhost:8083/connectors

curl -X POST -H "Content-Type: application/json" --data @config/sink-users-json-withschema-01.json http://localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" --data @config/sink-users-json-withschema-02.json http://localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" --data @config/sink-users-json-noschema-01.json http://localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" --data @config/sink-users-json-noschema-02.json http://localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" --data @config/sink-users-json-noschema-03.json http://localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" --data @config/sink-users-avro-01.json http://localhost:8083/connectors
curl -X POST -H "Content-Type: application/json" --data @config/sink-users-avro-02.json http://localhost:8083/connectors


