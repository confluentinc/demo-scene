#!/bin/bash

source .env

kafkacat -b $CCLOUD_BROKER_HOST \
    -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN \
    -X sasl.username="$CCLOUD_API_KEY" -X sasl.password="$CCLOUD_API_SECRET" \
    -X ssl.ca.location=/usr/local/etc/openssl/cert.pem -X api.version.request=true \
    -C -c1 -o -1 -t pcap -u |jq '.timestamp'|sed -e 's/"//g' | sed 's/[0-9][0-9][0-9]$//g' | xargs -Ifoo date -j -f %s foo
