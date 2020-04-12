#!/bin/bash

source .env

echo 'Now              : '  $(date)
ten_minutes_ago=$(date --date '-10 min' "+%s")
echo 'Ten minutes ago  : '  $(date -d @$ten_minutes_ago)

latest_ts=$(docker exec -it kafkacat kafkacat -b $CCLOUD_BROKER_HOST \
    -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN \
    -X sasl.username="$CCLOUD_API_KEY" -X sasl.password="$CCLOUD_API_SECRET" \
    -X api.version.request=true \
    -C -c1 -o -1 -t pcap -u |jq '.timestamp' |sed -e 's/"//g' | sed 's/[0-9][0-9][0-9]$//g' )

if [ -z $latest_ts ]; then
	echo "TS is empty"
	echo '{"chat_id": "-364377679", "text": "❌pcap ingest check failed. Latest ingest time is empty", "disable_notification": false}"' |\
	curl -X POST \
	     -H 'Content-Type: application/json' \
	     -d @- \
	     https://api.telegram.org/bot853879934:AAGGIOvd9JZDUuDfM-QVl7U3w32CZ_pxXfo/sendMessage
else
	echo 'Latest timestamp : '  $(date -d @$latest_ts)

	if [ $latest_ts -lt $ten_minutes_ago ]; then 
		echo "Ingest has stalled"
		echo '{"chat_id": "-364377679", "text": "❌pcap ingest has stalled. Latest ingest time is ' $(date -d @$latest_ts)'", "disable_notification": false}"' |\
		curl -X POST \
		     -H 'Content-Type: application/json' \
		     -d @- \
		     https://api.telegram.org/bot853879934:AAGGIOvd9JZDUuDfM-QVl7U3w32CZ_pxXfo/sendMessage
	else
		echo '{"chat_id": "-364377679", "text": "✅pcap ingest looks good. Latest ingest time is ' $(date -d @$latest_ts)'", "disable_notification": true}"' |\
		curl -X POST \
		     -H 'Content-Type: application/json' \
		     -d @- \
		     https://api.telegram.org/bot853879934:AAGGIOvd9JZDUuDfM-QVl7U3w32CZ_pxXfo/sendMessage
	fi
fi
