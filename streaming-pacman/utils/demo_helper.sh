
#!/bin/bash

DIR_DEMO_HELPER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

################################################################
# Downloading Utils from https://github.com/confluentinc/examples
#
# (This step will be skipped if the files are already in the utils folder)
################################################################
EXAMPLES_UTILS_URL="https://raw.githubusercontent.com/confluentinc/examples/latest/utils"

function check_wget() {
  if [[ $(type wget 2>&1) =~ "not found" ]]; then
    echo "'wget' is not found. Install wget and try again."
    return 1
  fi

  return 0
}

check_wget || exit 1


[ ! -f $DIR_DEMO_HELPER/config.env ] && wget $EXAMPLES_UTILS_URL/config.env -P $DIR_DEMO_HELPER
[ ! -f $DIR_DEMO_HELPER/helper.sh ] && wget $EXAMPLES_UTILS_URL/helper.sh -P $DIR_DEMO_HELPER
[ ! -f $DIR_DEMO_HELPER/ccloud_library.sh ] && wget $EXAMPLES_UTILS_URL/ccloud_library.sh -P $DIR_DEMO_HELPER


# Source library
source $DIR_DEMO_HELPER/helper.sh 
source $DIR_DEMO_HELPER/ccloud_library.sh


function ccloud::create_acls_fm_connectors_wildcard() {
    SERVICE_ACCOUNT_ID=$1
    CLUSTER=$2

    confluent kafka acl create --allow --service-account $SERVICE_ACCOUNT_ID --operation idempotent-write --cluster-scope --cluster $CLUSTER
    confluent kafka acl create --allow --service-account $SERVICE_ACCOUNT_ID --operation describe --cluster-scope --cluster $CLUSTER
    confluent kafka acl create --allow --service-account $SERVICE_ACCOUNT_ID --operation create --cluster-scope --cluster $CLUSTER

    return 0
}

function ccloud::create_topics(){
    CLUSTER=$1
    PARTITIONS=$2
    TOPICS_TO_CREATE=$3
    echo -e "\n# Will create the following topics: $TOPICS_TO_CREATE"
  for TOPIC in $TOPICS_TO_CREATE
  do
        echo -e "\n# Create new Kafka topic $TOPIC"
        echo "confluent kafka topic create \"$TOPIC\" --partitions $PARTITIONS"
        confluent kafka topic create "$TOPIC" --cluster $CLUSTER --partitions $PARTITIONS || true
        # In some cases I received an error 500 but the topic is created successfully anyway...
  done
}

ccloud::wait_for_data_in_topic() {
    local count=0
    while [[ "$count" -le 100 ]];do 
        count=$(timeout 10 confluent kafka topic consume -b $2 --cluster $1 | wc -l);
        echo "At least $count messages in the topic"
        #timeout 3 ccloud kafka topic consume -b $2 --cluster $1
        sleep 0.1 
    done 
}

ccloud::ccloud_stack_destroy(){
    if [ -z "$1" ]; then
        echo "ERROR: Must supply argument that is the client configuration file created from './ccloud_stack_create.sh'. (Is it in stack-configs/ folder?) "
        exit 1
    else
        CONFIG_FILE=$1
    fi

    PRESERVE_ENVIRONMENT="${PRESERVE_ENVIRONMENT:-false}"
    if [[ $PRESERVE_ENVIRONMENT == "false" ]]; then
        read -p "This script will destroy all the resources (including the Confluent Cloud environment) in $CONFIG_FILE.  Do you want to proceed? [y/n] " -n 1 -r
    else
        read -p "This script will destroy all the resources (except the Confluent Cloud environment) in $CONFIG_FILE.  Do you want to proceed? [y/n] " -n 1 -r
    fi
        
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]
    then
        exit 1
    fi

    ccloud::validate_ccloud_config $CONFIG_FILE || exit 1
    #DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
    ccloud::generate_configs $CONFIG_FILE > /dev/null
    source delta_configs/env.delta
    SERVICE_ACCOUNT_ID=$(ccloud::get_service_account $CLOUD_KEY) || exit 1

    #local environment_id=$(ccloud environment list -o json | jq -r 'map(select(.name | startswith("'"$ENVIRONMENT_NAME_PREFIX"'"))) | .[].id')
    #ccloud environment use $environment_id

    echo
    ccloud::destroy_ccloud_stack $SERVICE_ACCOUNT_ID

    echo
    echo "Tip: 'ccloud' CLI currently has no environment set"




}
