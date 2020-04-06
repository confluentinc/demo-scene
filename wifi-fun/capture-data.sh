#!/bin/bash

source $(dirname "$0")/.env

while [ 1 -eq 1 ];do 
	date
	sudo tshark -i mon0 \
		    -b duration:3600 -b files:2 -w /tmp/wireshark_tmp.pcap \
		    -T ek \
		    -l \
		    -e wlan.fc.type -e wlan.fc.type_subtype -e wlan_radio.channel \
		    -e wlan_radio.signal_dbm -e wlan_radio.duration -e wlan.ra \
		    -e wlan.ra_resolved -e wlan.da -e wlan.da_resolved \
		    -e wlan.ta -e wlan.ta_resolved -e wlan.sa \
		    -e wlan.sa_resolved -e wlan.staa -e wlan.staa_resolved \
		    -e wlan.tagged.all -e wlan.tag.vendor.data -e wlan.tag.vendor.oui.type \
		    -e wlan.tag.oui -e wlan.ssid -e wlan.country_info.code \
		    -e wps.device_name |\
	    grep timestamp|\
	    jq -c '{timestamp: .timestamp} + .layers' |\
	    kafkacat -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN -X api.version.request=true\
		    -b ${CCLOUD_BROKER_HOST}:9092 \
		    -X sasl.username="${CCLOUD_API_KEY}" \
		    -X sasl.password="${CCLOUD_API_SECRET}" \
		    -P \
            -t pcap 

	echo "process bombed out. sleeping and then looping"
	sleep 30
done
