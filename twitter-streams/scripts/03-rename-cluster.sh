#!/bin/bash

echo -e "\nRename the cluster in Control Center:"
# If you have 'jq'
curl -X PATCH  -H "Content-Type: application/merge-patch+json" -d '{"displayName":"Kafka Conferences Tour 🚀"}' http://localhost:9021/2.0/clusters/kafka/$(curl -X GET http://localhost:9021/2.0/clusters/kafka/ | jq --raw-output .[0].clusterId)
# If you don't have 'jq'
#curl -X PATCH  -H "Content-Type: application/merge-patch+json" -d '{"displayName":"Kafka Raleigh"}' http://localhost:9021/2.0/clusters/kafka/$(curl -X get http://localhost:9021/2.0/clusters/kafka/ | awk -v FS="(clusterId\":\"|\",\"displayName)" '{print $2}' )

