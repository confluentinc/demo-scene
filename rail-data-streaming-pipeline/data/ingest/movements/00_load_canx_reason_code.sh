#!/usr/bin/env bash

docker exec kafkacat \
  kafkacat -b broker:29092 -t canx_reason_code -P -K: -l /data/ingest/movements/canx_reason_code.dat

echo "Sample message read from the topic:"

docker exec kafkacat sh -c "\
  kafkacat -b broker:29092 -t canx_reason_code -C -c1 -J | \
  jq '.'"
  