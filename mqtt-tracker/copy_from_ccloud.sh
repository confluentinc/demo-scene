#!/bin/bash

source .env

kafkacat -b $CCLOUD_BROKER_HOST \
    -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN \
    -X sasl.username="$CCLOUD_API_KEY" -X sasl.password="$CCLOUD_API_SECRET" \
    -X ssl.ca.location=/usr/local/etc/openssl/cert.pem -X api.version.request=true \
    -X auto.offset.reset=earliest
    -G copy_to_local_00 data_mqtt -K: | \
kafkacat -b localhost:9092,localhost:19092,localhost:29092 \
    -t data_mqtt_ccloud_kc_00 \
    -K: -P 
