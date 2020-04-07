#!/bin/bash

echo "Kafka:"

kafkacat -b localhost:9092 \
    -C -c1 -o -1 -t pcap -u |jq '.timestamp'|sed -e 's/"//g' | sed 's/[0-9][0-9][0-9]$//g' | xargs -Ifoo date -d @foo

echo "Elasticsearch:"

curl -s http://localhost:9200/pcap/_search \
    -H 'content-type: application/json' \
    -d '{ "size": 1, "sort": [ { "timestamp": { "order": "desc" } } ] }' |\
    jq '.hits.hits[]._source.timestamp'|sed -e 's/"//g' | sed 's/[0-9][0-9][0-9]$//g' | xargs -Ifoo date -d @foo
