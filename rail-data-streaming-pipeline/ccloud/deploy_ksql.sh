#!/bin/bash

function deploy_ccloud_ksql {
  echo "Deploying "$1
  grep -v '^--' $1 | \
  tr '\n' ' ' | \
  sed 's/;/;\'$'\n''/g' | \
  sed 's/'\''/'"'"'/g' | \
  sed 's/"/\\\\"/g' | \
  while read stmt; do
      echo '------'

      if [ ${#stmt} -gt 0 ] ; then
        echo '{"ksql":"'"$stmt"'", "streamsProperties": { "ksql.streams.auto.offset.reset": "earliest" }}' | \
        curl --silent --show-error \
            -u $CCLOUD_KSQL_API_KEY:$CCLOUD_KSQL_API_SECRET \
            -X "POST" $CCLOUD_KSQL_ENDPOINT"/ksql" \
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
        curl --silent --show-error \
            -u $CCLOUD_KSQL_API_KEY:$CCLOUD_KSQL_API_SECRET \
            -X "POST" $CCLOUD_KSQL_ENDPOINT"/query-stream" \
            -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
            -d @- | \
        jq
      fi
  done
}
