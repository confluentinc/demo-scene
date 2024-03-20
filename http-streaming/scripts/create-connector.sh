#!/bin/bash

CLUSTER_ID=`confluent kafka cluster list -o json | jq -c -r '.[] | select(.name | contains("http-streaming_kafka-cluster")) | .id'`

API_KEY_JSON=`confluent api-key create --resource $CLUSTER_ID -o json`

API_KEY=`jq -r '.api_key' <<< $API_KEY_JSON`
API_SECRET=`jq -r '.api_secret' <<< $API_KEY_JSON`

CONNECTOR_CONFIG=$(cat <<EOF
{
  "name": "OpenSkyFlights",
   "config": {
      "connector.class": "HttpSource",
      "kafka.auth.mode": "KAFKA_API_KEY",
      "kafka.api.key": "$API_KEY",
      "kafka.api.secret": "$API_SECRET",
      "topic.name.pattern": "all_flights",
      "schema.context.name": "default",
      "url": "https://opensky-network.org/api/states/all",
      "http.request.method": "GET",
      "http.request.parameters": "lamin=45.8389&lomin=5.9962&lamax=47.8229&lomax=10.5226",
      "http.initial.offset": "0",
      "http.offset.mode": "SIMPLE_INCREMENTING",
      "request.interval.ms": "60000",
      "max.retries": "10",
      "retry.backoff.ms": "3000",
      "retry.on.status.codes": "400-",
      "http.request.headers.separator": "|",
      "http.request.parameters.separator": "&",
      "auth.type": "none",
      "https.ssl.enabled": "false",
      "output.data.format": "AVRO",
      "tasks.max": "1"
  }
}
)

TEMP_FILE=`mktemp`

echo $CONNECTOR_CONFIG >> $TEMP_FILE

confluent connect cluster create --config-file $TEMP_FILE -o json
