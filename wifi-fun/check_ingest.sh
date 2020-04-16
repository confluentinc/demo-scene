#!/bin/bash
#
# @rmoff 16 April 2020
#
# -------------

# .env should look like: 
# CCLOUD_BROKER_HOST=xxxxxx
# CCLOUD_API_KEY=xxxxxx
# CCLOUD_API_SECRET=xxxxxx
# TELEGRAM_BOT_TOKEN=xxxx

source .env
CHAT_ID=-468250841

#---------

echo 'Now              : '  $(docker run --rm ubuntu date)
ten_minutes_ago=$(docker run --rm ubuntu date --date '-10 min' "+%s")
echo 'Ten minutes ago  : '  $(docker run --rm ubuntu date -d @$ten_minutes_ago)

latest_ts=$(docker run --rm edenhill/kafkacat:1.5.0 kafkacat -b $CCLOUD_BROKER_HOST \
    -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN \
    -X sasl.username="$CCLOUD_API_KEY" -X sasl.password="$CCLOUD_API_SECRET" \
    -X api.version.request=true \
    -C -c1 -o -1 -t pcap -u -f %T| sed 's/[0-9][0-9][0-9]$//g' )

if [ -z $latest_ts ]; then
	echo "TS is empty"
	echo '{"chat_id": "'$CHAT_ID'", "text": "❌pcap ingest check failed. Latest ingest time is empty", "disable_notification": false}"' |\
	curl -s -X POST \
	     -H 'Content-Type: application/json' \
	     -d @- \
	     https://api.telegram.org/bot$TELEGRAM_BOT_TOKEN/sendMessage | jq '.'
else
	echo 'Latest timestamp : '  $(docker run --rm ubuntu date -d @$latest_ts)

	if [ $latest_ts -lt $ten_minutes_ago ]; then 
		echo "Ingest has stalled"
		echo '{"chat_id": "'$CHAT_ID'", "text": "❌pcap ingest has stalled. Latest ingest time is ' $(docker run --rm ubuntu date -d @$latest_ts)'", "disable_notification": false}"' |\
		curl -s -X POST \
		     -H 'Content-Type: application/json' \
		     -d @- \
		     https://api.telegram.org/bot$TELEGRAM_BOT_TOKEN/sendMessage | jq '.'
	else
		echo '{"chat_id": "'$CHAT_ID'", "text": "✅pcap ingest looks good. Latest ingest time is ' $(docker run --rm ubuntu date -d @$latest_ts)'", "disable_notification": true}"' |\
		curl -s -X POST \
		     -H 'Content-Type: application/json' \
		     -d @- \
		     https://api.telegram.org/bot$TELEGRAM_BOT_TOKEN/sendMessage | jq '.'
	fi
fi
