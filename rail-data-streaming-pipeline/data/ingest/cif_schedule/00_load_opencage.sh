#!/usr/bin/env bash

jq '. + {"rest_query": .request.query} | del(.request)' -c opencage.json | \
  docker exec -i kafkacat \
    kafkacat -b broker:29092 -t opencage -P

echo "Sample message read from the topic:"

docker exec kafkacat sh -c "\
  kafkacat -b broker:29092 -t opencage -C -c1 -J | \
  jq '.'"
  