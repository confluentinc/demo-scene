#!/bin/bash

#################################################################
# Initialization
#################################################################

PRJ_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
UTILS_DIR="${PRJ_DIR}/utils"
export EXAMPLE="streaming-pacman"

# Source library
source $UTILS_DIR/helper.sh
source $UTILS_DIR/ccloud_library.sh

# Source demo-specific configurations
source config/demo.cfg

#################################################################
# Source CCloud configurations
#################################################################
DELTA_CONFIGS_DIR=delta_configs
source $DELTA_CONFIGS_DIR/env.delta

#################################################################
# Confluent Cloud ksqlDB application
#################################################################
echo -e "\nConfluent Cloud ksqlDB application endpoin $KSQLDB_ENDPOINT\n"
ccloud::validate_ksqldb_up "$KSQLDB_ENDPOINT" || exit 1

# Create required topics and ACLs
for TOPIC in $TOPICS_TO_CREATE
do
  echo -e "\n# Create new Kafka topic $TOPIC"
  ccloud kafka topic create "$TOPIC"   
done

ccloud ksql app list

ksqlDBAppId=$(ccloud ksql app list | grep "$KSQLDB_ENDPOINT" | awk '{print $1}')
echo "ksqldb app id: ksqlDBAppId"
ccloud ksql app configure-acls $ksqlDBAppId $TOPICS_TO_CREATE

SERVICE_ACCOUNT_ID=$(ccloud kafka cluster list -o json | jq -r '.[0].name' | awk -F'-' '{print $4;}')
for TOPIC in $TOPICS_TO_CREATE
do
  ccloud kafka acl create --allow --service-account $SERVICE_ACCOUNT_ID --operation WRITE --topic $TOPIC      
done

# Submit KSQL queries
echo -e "\nSubmit KSQL queries\n"
properties='"ksql.streams.auto.offset.reset":"earliest","ksql.streams.cache.max.bytes.buffering":"0"'
while read ksqlCmd; do
  echo -e "\n$ksqlCmd\n"
  response=$(curl -X POST $KSQLDB_ENDPOINT/ksql \
       -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
       -u $KSQLDB_BASIC_AUTH_USER_INFO \
       --silent \
       -d @<(cat <<EOF
{
  "ksql": "$ksqlCmd",
  "streamsProperties": {$properties}
}
EOF
))
  echo $response
  if [[ ! "$response" =~ "SUCCESS" ]]; then
    echo -e "\nERROR: KSQL command '$ksqlCmd' did not include \"SUCCESS\" in the response.  Please troubleshoot."
    exit 1
  fi
done <statements.sql
echo -e "\nSleeping 20 seconds after submitting KSQL queries\n"
sleep 20

exit 0
