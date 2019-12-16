#!/usr/bin/env bash

set +x

source .env

curl -s 'http://192.168.10.140:1400/support/review' | \
    xq -c '.ZPNetworkInfo.ZPSupportInfo[] | {ZPInfo: .ZPInfo.ZoneName, data: [(.File[] | select (."@name" == "/proc/ath_rincon/status") | ."#text" | split("\n")[] | select((. | contains("OFDM")) or (.|contains("Noise")) or (.|contains("PHY"))))], rawdata: (.File[] | select (."@name" == "/proc/ath_rincon/status") | ."#text")}' | \
    docker run --interactive --rm edenhill/kafkacat:1.5.0 \
        -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN \
        -X ssl.ca.location=./etc/ssl/cert.pem -X api.version.request=true \
        -b ${CCLOUD_BROKER_HOST}:9092 \
        -X sasl.username="${CCLOUD_API_KEY}" \
        -X sasl.password="${CCLOUD_API_SECRET}" \
        -t sonos-metrics -P