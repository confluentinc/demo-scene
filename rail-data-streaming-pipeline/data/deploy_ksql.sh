#!/bin/bash

function deploy_ksql {
  echo "Deploying "$1
  grep -v '^--' $1 | \
  tr '\n' ' ' | \
  sed 's/;/;\'$'\n''/g' | \
  sed 's/"/\\\\"/g' | \
  while read stmt; do
      echo '------'
      echo "$stmt"

      if [ ${#stmt} -gt 0 ] ; then
        echo '{"ksql":"'"$stmt"'", "streamsProperties": { "ksql.streams.auto.offset.reset": "earliest" }}' | \
        curl -s -X "POST" "http://localhost:8088/ksql" \
            -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
            -d @- | \
        jq
      fi
  done
  # Give ourselves chance to breath
  sleep 2
}

function query_ksql {
  echo "Querying "$1
  grep -v '^--' $1 | \
  tr '\n' ' ' | \
  sed 's/;/;\'$'\n''/g' | \
  sed 's/"/\\\\"/g' | \
  while read stmt; do
      echo '------'
      echo "$stmt"

      if [ ${#stmt} -gt 0 ] ; then
        echo '{"sql":"'"$stmt"'", "properties": { "ksql.streams.auto.offset.reset": "earliest" }}' | \
        curl --silent --show-error --http2 -X "POST" "http://localhost:8088/query-stream" \
            -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
            -d @- | \
        jq
      fi
  done
}
