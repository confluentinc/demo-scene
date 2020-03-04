#!/bin/bash

source .env

while [ 1 -eq 1 ] 
do
kafkacat -b $CCLOUD_BROKER_HOST \
    -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN \
    -X sasl.username="$CCLOUD_API_KEY" -X sasl.password="$CCLOUD_API_SECRET" \
    -X ssl.ca.location=/usr/local/etc/openssl/cert.pem -X api.version.request=true \
    -X auto.offset.reset=earliest \
    -K: \
    -G asgard03_copy_to_local_05 pcap | \
kafkacat -b localhost:9092,localhost:19092,localhost:29092 \
    -t pcap \
    -K: -P -T

sleep 30

done
