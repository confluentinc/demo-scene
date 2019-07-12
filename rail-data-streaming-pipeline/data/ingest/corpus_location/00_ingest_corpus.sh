#!/usr/bin/env bash

source $(dirname $(readlink -f $0))/../../set_credentials_env.sh

# This uses jq to explode the source array into individual 
# messages, since KSQL can't do that yet

curl -s -L -u "$NROD_USERNAME:$NROD_PASSWORD" http://datafeeds.networkrail.co.uk/ntrod/SupportingFileAuthenticate?type=CORPUS | \
  gunzip | \
  jq -c '.[][]' | \
  kafkacat -b localhost -P -t corpus
