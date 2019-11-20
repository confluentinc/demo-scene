#!/bin/bash

source .env

echo "Waiting for Kafka Connect to start listening on localhost:8083 ⏳"
while : ; do
    curl_status=$(curl -s -o /dev/null -w %{http_code} http://localhost:8083/connectors)
    echo -e $(date) " Kafka Connect listener HTTP state: " $curl_status " (waiting for 200)"
    if [ $curl_status -eq 200 ] ; then
    break
    fi
    sleep 5 
done
#
curl -X PUT -H  "Content-Type:application/json" http://localhost:8083/connectors/source-mqtt-01/config \
    -d '{
    "connector.class" : "io.confluent.connect.mqtt.MqttSourceConnector",
    "mqtt.server.uri" : "${file:/data/mqtt.credentials:MQTT_URL}",
    "mqtt.password" : "${file:/data/mqtt.credentials:MQTT_PASSWORD}",
    "mqtt.username" : "${file:/data/mqtt.credentials:MQTT_USERNAME}",
    "mqtt.topics" : "owntracks/#",
    "kafka.topic" : "data_mqtt",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "org.apache.kafka.connect.converters.ByteArrayConverter",
    "tasks.max" : "1",
    "confluent.topic.bootstrap.servers" : "kafka-1:39092,kafka-2:49092,kafka-3:59092",
    "confluent.topic.replication.factor" : "1"
    }'
