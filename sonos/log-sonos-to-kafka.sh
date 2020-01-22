#!/usr/bin/env bash

set -x

while [ 1 -eq 1 ];do 
    curl -s 'http://192.168.10.98:1400/support/review' | \
        xq -c '.ZPNetworkInfo.ZPSupportInfo[] | 
            {ZPInfo: .ZPInfo.ZoneName, 
            data: [(.File[] | 
                    select (."@name" == "/proc/ath_rincon/status") | 
                    ."#text" | 
                    split("\n")[] | 
                    select((. | contains("OFDM")) or (.|contains("Noise")) or (.|contains("PHY"))))]
                }' | \
            docker exec -i kafkacat kafkacat \
                -b kafka-1:39092,kafka-2:49092,kafka-3:59092 \
                -t sonos-metrics -P -T | jq '.'
    sleep 20
done
