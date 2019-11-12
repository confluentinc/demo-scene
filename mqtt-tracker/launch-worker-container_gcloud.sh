#!/bin/bash
# 
# Robin Moffatt <robin@rmoff.net>
# 12 November 2019
#
# This script will launch a Kafka Connect worker on GCP connecting to Confluent Cloud. 
# It will install a connector plugin and once the worker is running submit a connector 
# configuration to it. 
#
# Since Kafka Connect configuration is stored in Kafka itself, the state for this worker
# is not tied to its instance but to the Kafka cluster (Confluent Cloud). This script 
# uses the timestamp to uniquely label the instance of the Kafka Connect cluster's topics
# for this reason. 
#
# Use: 
# ----
# 
# 1. Populate .env with the following: 
#   CONFLUENTPLATFORM_VERSION=5.4.0-beta1
#   
#   CCLOUD_BROKER_HOST=
#   CCLOUD_API_KEY=
#   CCLOUD_API_SECRET=
#   CCLOUD_SCHEMA_REGISTRY_URL=
#   CCLOUD_SCHEMA_REGISTRY_API_KEY=
#   CCLOUD_SCHEMA_REGISTRY_API_SECRET=
#
# 2. Install the gcloud CLI and run `gcloud init` to set up authentication
#
# 3. Tweak the connector config, plugin name, target topic name etc.

# -------------------
# Epoch will be our unique ID. 
# If you're need something more unique, you can write it here ;)
epoch=$(date +%s)

# Load credentials
source .env

# Manually create the target topic that Connect will write to

cat > /tmp/config-${epoch}.properties <<EOF
ssl.endpoint.identification.algorithm=https
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="${CCLOUD_API_KEY}" password="${CCLOUD_API_SECRET}";
security.protocol=SASL_SSL
request.timeout.ms=20000
retry.backoff.ms=500
EOF

docker run --rm --volume /tmp/config-${epoch}.properties:/tmp/config.properties \
    confluentinc/cp-kafka:${CONFLUENTPLATFORM_VERSION} /usr/bin/kafka-topics \
    --command-config /tmp/config.properties \
    --bootstrap-server ${CCLOUD_BROKER_HOST}:9092 \
    --create \
    --topic data_mqtt \
    --partitions 6 \
    --replication-factor 3

# Build the properties file
PROPERTIES_FILE=/tmp/connect-worker-${epoch}_gcloud_env.properties
echo $PROPERTIES_FILE
# Need do it this way to interpolate some of the values 
# (and passing env vars natively in gcloud CLI is a joke)
cat > ${PROPERTIES_FILE}<<EOF
CONNECT_LOG4J_APPENDER_STDOUT_LAYOUT_CONVERSIONPATTERN=[%d] %p %X{connector.context}%m (%c:%L)%n
CONNECT_CUB_KAFKA_TIMEOUT=300  
CONNECT_BOOTSTRAP_SERVERS=${CCLOUD_BROKER_HOST}:9092
CONNECT_REST_ADVERTISED_HOST_NAME=kafka-connect-01
CONNECT_REST_PORT=8083  
CONNECT_GROUP_ID=kafka-connect-group-${epoch}
CONNECT_CONFIG_STORAGE_TOPIC=_kafka-connect-group-${epoch}-configs  
CONNECT_OFFSET_STORAGE_TOPIC=_kafka-connect-group-${epoch}-offsets  
CONNECT_STATUS_STORAGE_TOPIC=_kafka-connect-group-${epoch}-status  
CONNECT_KEY_CONVERTER=io.confluent.connect.avro.AvroConverter  
CONNECT_KEY_CONVERTER_SCHEMA_REGISTRY_URL=${CCLOUD_SCHEMA_REGISTRY_URL}
CONNECT_KEY_CONVERTER_BASIC_AUTH_CREDENTIALS_SOURCE=USER_INFO
CONNECT_KEY_CONVERTER_SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO=${CCLOUD_SCHEMA_REGISTRY_API_KEY}:${CCLOUD_SCHEMA_REGISTRY_API_SECRET}
CONNECT_VALUE_CONVERTER=io.confluent.connect.avro.AvroConverter  
CONNECT_VALUE_CONVERTER_SCHEMA_REGISTRY_URL=${CCLOUD_SCHEMA_REGISTRY_URL}
CONNECT_VALUE_CONVERTER_BASIC_AUTH_CREDENTIALS_SOURCE=USER_INFO
CONNECT_VALUE_CONVERTER_SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO=${CCLOUD_SCHEMA_REGISTRY_API_KEY}:${CCLOUD_SCHEMA_REGISTRY_API_SECRET}
CONNECT_INTERNAL_KEY_CONVERTER=org.apache.kafka.connect.json.JsonConverter
CONNECT_INTERNAL_VALUE_CONVERTER=org.apache.kafka.connect.json.JsonConverter
CONNECT_LOG4J_ROOT_LOGLEVEL=INFO
CONNECT_LOG4J_LOGGERS=org.apache.kafka.connect.runtime.rest=WARN,org.reflections=ERROR
CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR=3
CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR=3
CONNECT_STATUS_STORAGE_REPLICATION_FACTOR=3
CONNECT_PLUGIN_PATH=/usr/share/java,/usr/share/confluent-hub-components/
CONNECT_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM=https
CONNECT_SASL_MECHANISM=PLAIN
CONNECT_SECURITY_PROTOCOL=SASL_SSL
CONNECT_SASL_JAAS_CONFIG=org.apache.kafka.common.security.plain.PlainLoginModule required username="${CCLOUD_API_KEY}" password="${CCLOUD_API_SECRET}";
CONNECT_CONSUMER_SECURITY_PROTOCOL=SASL_SSL
CONNECT_CONSUMER_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM=https
CONNECT_CONSUMER_SASL_MECHANISM=PLAIN
CONNECT_CONSUMER_SASL_JAAS_CONFIG=org.apache.kafka.common.security.plain.PlainLoginModule required username="${CCLOUD_API_KEY}" password="${CCLOUD_API_SECRET}";
CONNECT_PRODUCER_SECURITY_PROTOCOL=SASL_SSL
CONNECT_PRODUCER_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM=https
CONNECT_PRODUCER_SASL_MECHANISM=PLAIN
CONNECT_PRODUCER_SASL_JAAS_CONFIG=org.apache.kafka.common.security.plain.PlainLoginModule required username="${CCLOUD_API_KEY}" password="${CCLOUD_API_SECRET}";
EOF

gcloud beta compute \
	--project=devx-testing instances create-with-container rmoff-connect-mqtt-${epoch} \
	--machine-type=n1-standard-1 \
	--subnet=default \
	--metadata=google-logging-enabled=true \
	--maintenance-policy=MIGRATE \
	--image=cos-stable-77-12371-114-0 \
	--image-project=cos-cloud \
    --no-scopes \
    --no-service-account \
	--boot-disk-size=10GB \
	--boot-disk-type=pd-standard \
	--boot-disk-device-name=rmoff-connect-mqtt-${epoch} \
	--container-restart-policy=always \
	--labels=container-vm=cos-stable-77-12371-114-0 \
	--container-image=confluentinc/cp-kafka-connect:${CONFLUENTPLATFORM_VERSION} \
    --container-env-file=${PROPERTIES_FILE} \
	--container-command=bash \
	--container-arg=-c \
	--container-arg='set -x
        echo "Installing connector plugins" 
        confluent-hub install --no-prompt confluentinc/kafka-connect-mqtt:1.2.3
        #
        echo "Launching Kafka Connect worker"
        /etc/confluent/docker/run & 
        #
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
        curl -s -X PUT -H  "Content-Type:application/json" http://localhost:8083/connectors/source-mqtt/config \
            -d '"'"'{  
            "connector.class" : "io.confluent.connect.mqtt.MqttSourceConnector", 
            "mqtt.server.uri" : "'${MQTT_URL}'", 
            "mqtt.password" : "'${MQTT_PASSWORD}'", 
            "mqtt.username" : "'${MQTT_USERNAME}'", 
            "mqtt.topics" : "owntracks/#", 
            "kafka.topic" : "data_mqtt", 
            "key.converter": "org.apache.kafka.connect.storage.StringConverter", 
            "value.converter": "org.apache.kafka.connect.converters.ByteArrayConverter", 
            "tasks.max" : "1", 
            "confluent.topic.bootstrap.servers" : "'${CCLOUD_BROKER_HOST}':9092", 
            "confluent.topic.sasl.jaas.config" : "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"'${CCLOUD_API_KEY}'\" password=\"'${CCLOUD_API_SECRET}'\";", 
            "confluent.topic.security.protocol": "SASL_SSL", 
            "confluent.topic.ssl.endpoint.identification.algorithm": "https", 
            "confluent.topic.sasl.mechanism": "PLAIN" 
            }'"'"'
        #    
        sleep infinity'

# Wossup, credential leakage? 
rm ${PROPERTIES_FILE}
rm /tmp/config-${epoch}.properties