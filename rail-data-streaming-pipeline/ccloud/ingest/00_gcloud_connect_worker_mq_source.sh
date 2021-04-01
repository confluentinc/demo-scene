gcloud compute instances create-with-container rmoff-connect-source-trains-v2-02 \
        --zone=us-east1-b \
        --tags kafka-connect \
      	--metadata=google-logging-enabled=true \
        --container-image confluentinc/cp-kafka-connect-base:6.1.0 \
        --container-restart-policy=never \
        --container-command=/bin/bash \
        --container-arg=-c \
        --container-arg='set -x
        #
        # Set the environment variables
        export CONNECT_CUB_KAFKA_TIMEOUT=300
        export CONNECT_BOOTSTRAP_SERVERS=<CCLOUD_BROKER>
        export CONNECT_REST_ADVERTISED_HOST_NAME=rmoff-connect-source_trains_v2_02
        export CONNECT_REST_PORT=8083
        export CONNECT_GROUP_ID=kafka-connect-group-gcp-v02
        export CONNECT_CONFIG_STORAGE_TOPIC=_kafka-connect-group-gcp-v02-configs
        export CONNECT_OFFSET_STORAGE_TOPIC=_kafka-connect-group-gcp-v02-offsets
        export CONNECT_STATUS_STORAGE_TOPIC=_kafka-connect-group-gcp-v02-status
        export CONNECT_KEY_CONVERTER=io.confluent.connect.avro.AvroConverter
        export CONNECT_KEY_CONVERTER_SCHEMA_REGISTRY_URL=<CCLOUD_SCHEMA_REGISTRY_ENDPOINT>
        export CONNECT_KEY_CONVERTER_BASIC_AUTH_CREDENTIALS_SOURCE=USER_INFO
        export CONNECT_KEY_CONVERTER_SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO="<CCLOUD_SCHEMA_REGISTRY_API_KEY>:<CCLOUD_SCHEMA_REGISTRY_API_SECRET>"
        export CONNECT_VALUE_CONVERTER=io.confluent.connect.avro.AvroConverter
        export CONNECT_VALUE_CONVERTER_SCHEMA_REGISTRY_URL=<CCLOUD_SCHEMA_REGISTRY_ENDPOINT>
        export CONNECT_VALUE_CONVERTER_BASIC_AUTH_CREDENTIALS_SOURCE=USER_INFO
        export CONNECT_VALUE_CONVERTER_SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO="<CCLOUD_SCHEMA_REGISTRY_API_KEY>:<CCLOUD_SCHEMA_REGISTRY_API_SECRET>"
        export CONNECT_LOG4J_ROOT_LOGLEVEL=INFO
        export CONNECT_LOG4J_LOGGERS=org.apache.kafka.connect.runtime.rest=WARN,org.reflections=ERROR
        export CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR=3
        export CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR=3
        export CONNECT_STATUS_STORAGE_REPLICATION_FACTOR=3
        export CONNECT_PLUGIN_PATH=/usr/share/java,/usr/share/confluent-hub-components/
        export CONNECT_RETRY_BACKOFF_MS=500
        export CONNECT_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM=https
        export CONNECT_SASL_MECHANISM=PLAIN
        export CONNECT_SECURITY_PROTOCOL=SASL_SSL
        export CONNECT_CONSUMER_SECURITY_PROTOCOL=SASL_SSL
        export CONNECT_CONSUMER_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM=https
        export CONNECT_CONSUMER_SASL_MECHANISM=PLAIN
        export CONNECT_CONSUMER_RETRY_BACKOFF_MS=500
        export CONNECT_PRODUCER_SECURITY_PROTOCOL=SASL_SSL
        export CONNECT_PRODUCER_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM=https
        export CONNECT_PRODUCER_SASL_MECHANISM=PLAIN
        export CONNECT_PRODUCER_RETRY_BACKOFF_MS=500
        export CONNECT_SASL_JAAS_CONFIG="org.apache.kafka.common.security.plain.PlainLoginModule required username=\"<CCLOUD_API_KEY>\" password=\"<CCLOUD_API_SECRET>\";"
        export CONNECT_CONSUMER_SASL_JAAS_CONFIG="org.apache.kafka.common.security.plain.PlainLoginModule required username=\"<CCLOUD_API_KEY>\" password=\"<CCLOUD_API_SECRET>\";"
        export CONNECT_PRODUCER_SASL_JAAS_CONFIG="org.apache.kafka.common.security.plain.PlainLoginModule required username=\"<CCLOUD_API_KEY>\" password=\"<CCLOUD_API_SECRET>\";"
        #
        echo "Installing connector plugins"
        confluent-hub install --no-prompt confluentinc/kafka-connect-activemq:11.0.2
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
        echo -e "\n--\n+> Creating Kafka Connect source connectors"
        curl -s -X PUT -H  "Content-Type:application/json" \
        http://localhost:8083/connectors/source-activemq-networkrail-TRAIN_MVT_EA_TOC-01/config \
            -d '"'"'{  
                "connector.class"                                      : "io.confluent.connect.activemq.ActiveMQSourceConnector",
                "activemq.url"                                         : "tcp://datafeeds.networkrail.co.uk:61619",
                "activemq.username"                                    : "<NROD_USERNAME>",
                "activemq.password"                                    : "<NROD_PASSWORD>",
                "jms.destination.type"                                 : "topic",
                "jms.destination.name"                                 : "TRAIN_MVT_EA_TOC",
                "kafka.topic"                                          : "networkrail_train_mvt_v01",
                "topic.creation.default.partitions"                    : 6,
                "topic.creation.default.replication.factor"            : 3,
                "confluent.license"                                    : "",
                "confluent.topic.bootstrap.servers"                    : "<CCLOUD_BROKER>",
                "confluent.topic.sasl.jaas.config"                     : "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"'<CCLOUD_API_KEY>'\" password=\"'<CCLOUD_API_SECRET>'\";",
                "confluent.topic.security.protocol"                    : "SASL_SSL",
                "confluent.topic.ssl.endpoint.identification.algorithm": "https",
                "confluent.topic.sasl.mechanism"                       : "PLAIN",
                "confluent.topic.request.timeout.ms"                   : "20000",
                "confluent.topic.retry.backoff.ms"                     : "500"
            }'"'"'
        curl -s -X PUT -H  "Content-Type:application/json" \
        http://localhost:8083/connectors/source-activemq-networkrail-TRAIN_MVT_HB_TOC-01/config \
            -d '"'"'{  
                "connector.class"                                      : "io.confluent.connect.activemq.ActiveMQSourceConnector",
                "activemq.url"                                         : "tcp://datafeeds.networkrail.co.uk:61619",
                "activemq.username"                                    : "<NROD_USERNAME>",
                "activemq.password"                                    : "<NROD_PASSWORD>",
                "jms.destination.type"                                 : "topic",
                "jms.destination.name"                                 : "TRAIN_MVT_HB_TOC",
                "kafka.topic"                                          : "networkrail_train_mvt_v01",
                "topic.creation.default.partitions"                    : 6,
                "topic.creation.default.replication.factor"            : 3,
                "confluent.license"                                    : "",
                "confluent.topic.bootstrap.servers"                    : "<CCLOUD_BROKER>",
                "confluent.topic.sasl.jaas.config"                     : "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"'<CCLOUD_API_KEY>'\" password=\"'<CCLOUD_API_SECRET>'\";",
                "confluent.topic.security.protocol"                    : "SASL_SSL",
                "confluent.topic.ssl.endpoint.identification.algorithm": "https",
                "confluent.topic.sasl.mechanism"                       : "PLAIN",
                "confluent.topic.request.timeout.ms"                   : "20000",
                "confluent.topic.retry.backoff.ms"                     : "500"
            }'"'"'
        curl -s -X PUT -H  "Content-Type:application/json" \
        http://localhost:8083/connectors/source-activemq-networkrail-TRAIN_MVT_EM_TOC-01/config \
            -d '"'"'{  
                "connector.class"                                      : "io.confluent.connect.activemq.ActiveMQSourceConnector",
                "activemq.url"                                         : "tcp://datafeeds.networkrail.co.uk:61619",
                "activemq.username"                                    : "<NROD_USERNAME>",
                "activemq.password"                                    : "<NROD_PASSWORD>",
                "jms.destination.type"                                 : "topic",
                "jms.destination.name"                                 : "TRAIN_MVT_EM_TOC",
                "kafka.topic"                                          : "networkrail_train_mvt_v01",
                "topic.creation.default.partitions"                    : 6,
                "topic.creation.default.replication.factor"            : 3,
                "confluent.license"                                    : "",
                "confluent.topic.bootstrap.servers"                    : "<CCLOUD_BROKER>",
                "confluent.topic.sasl.jaas.config"                     : "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"'<CCLOUD_API_KEY>'\" password=\"'<CCLOUD_API_SECRET>'\";",
                "confluent.topic.security.protocol"                    : "SASL_SSL",
                "confluent.topic.ssl.endpoint.identification.algorithm": "https",
                "confluent.topic.sasl.mechanism"                       : "PLAIN",
                "confluent.topic.request.timeout.ms"                   : "20000",
                "confluent.topic.retry.backoff.ms"                     : "500"
            }'"'"'
        curl -s -X PUT -H  "Content-Type:application/json" \
        http://localhost:8083/connectors/source-activemq-networkrail-TRAIN_MVT_ED_TOC-01/config \
            -d '"'"'{  
                "connector.class"                                      : "io.confluent.connect.activemq.ActiveMQSourceConnector",
                "activemq.url"                                         : "tcp://datafeeds.networkrail.co.uk:61619",
                "activemq.username"                                    : "<NROD_USERNAME>",
                "activemq.password"                                    : "<NROD_PASSWORD>",
                "jms.destination.type"                                 : "topic",
                "jms.destination.name"                                 : "TRAIN_MVT_ED_TOC",
                "kafka.topic"                                          : "networkrail_train_mvt_v01",
                "topic.creation.default.partitions"                    : 6,
                "topic.creation.default.replication.factor"            : 3,
                "confluent.license"                                    : "",
                "confluent.topic.bootstrap.servers"                    : "<CCLOUD_BROKER>",
                "confluent.topic.sasl.jaas.config"                     : "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"'<CCLOUD_API_KEY>'\" password=\"'<CCLOUD_API_SECRET>'\";",
                "confluent.topic.security.protocol"                    : "SASL_SSL",
                "confluent.topic.ssl.endpoint.identification.algorithm": "https",
                "confluent.topic.sasl.mechanism"                       : "PLAIN",
                "confluent.topic.request.timeout.ms"                   : "20000",
                "confluent.topic.retry.backoff.ms"                     : "500"
            }'"'"'
        #    
        sleep infinity'
    
