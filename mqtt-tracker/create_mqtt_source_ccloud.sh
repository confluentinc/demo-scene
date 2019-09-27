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
echo -e "\n--\n+> Creating Kafka Connect MQTT source"
curl -X PUT -H  "Content-Type:application/json" http://localhost:8083/connectors/source-mqtt-01/config \
    -d '{
    "connector.class" : "io.confluent.connect.mqtt.MqttSourceConnector",
    "mqtt.server.uri" : "'$MQTT_URL'",
    "mqtt.password" : "'$MQTT_PASSWORD'",
    "mqtt.username" : "'$MQTT_USERNAME'",
    "mqtt.topics" : "owntracks/#",
    "kafka.topic" : "data_mqtt",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter",
    "value.converter": "org.apache.kafka.connect.converters.ByteArrayConverter",
    "tasks.max" : "1",
    "confluent.topic.bootstrap.servers" : "'$CCLOUD_BROKER_HOST':9092",
    "confluent.topic.sasl.jaas.config" : "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"'CCLOUD_API_KEY'\" password=\"'$CCLOUD_API_SECRET'\";",
    "confluent.topic.security.protocol": "SASL_SSL",
    "confluent.topic.ssl.endpoint.identification.algorithm": "https",
    "confluent.topic.sasl.mechanism": "PLAIN",
    "confluent.topic.request.timeout.ms": "20000",
    "confluent.topic.retry.backoff.ms": "500"
    }'
#