
#!/bin/bash

DIR_DEMO_HELPER="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

################################################################
# Downloading Utils from https://github.com/confluentinc/examples
#
# (This step will be skipped if the files are already in the utils folder)
################################################################
EXAMPLES_URL="https://raw.githubusercontent.com/confluentinc/examples/6.1.1-post"
EXAMPLES_UTILS_URL="${EXAMPLES_URL}/utils"

[ ! -f $DIR_DEMO_HELPER/helper.sh ] && wget $EXAMPLES_UTILS_URL/helper.sh -P $DIR_DEMO_HELPER
[ ! -f $DIR_DEMO_HELPER/ccloud_library.sh ] && wget $EXAMPLES_UTILS_URL/ccloud_library.sh -P $DIR_DEMO_HELPER
[ ! -f $DIR_DEMO_HELPER/config.env ] && wget $EXAMPLES_UTILS_URL/config.env -P $DIR_DEMO_HELPER

# Source library
source $DIR_DEMO_HELPER/helper.sh 
source $DIR_DEMO_HELPER/ccloud_library.sh


function ccloud::create_acls_fm_connectors_wildcard() {
    SERVICE_ACCOUNT_ID=$1
    CLUSTER=$2

    ccloud kafka acl create --allow --service-account $SERVICE_ACCOUNT_ID --operation idempotent-write --cluster-scope --cluster $CLUSTER
    ccloud kafka acl create --allow --service-account $SERVICE_ACCOUNT_ID --operation describe --cluster-scope --cluster $CLUSTER
    ccloud kafka acl create --allow --service-account $SERVICE_ACCOUNT_ID --operation create --cluster-scope --cluster $CLUSTER

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
        echo "ccloud kafka topic create \"$TOPIC\" --partitions $PARTITIONS"
        ccloud kafka topic create "$TOPIC" --cluster $CLUSTER --partitions $PARTITIONS || true
        # In some cases I received an error 500 but the topic is created successfully anyway...
  done
}

ccloud::wait_for_data_in_topic() {
    local count=0
    while [[ "$count" -le 100 ]];do 
        count=$(timeout 10 ccloud kafka topic consume -b $2 --cluster $1 | wc -l);
        echo "At least $count messages in the topic"
        #timeout 3 ccloud kafka topic consume -b $2 --cluster $1
        sleep 0.1 
    done 
}

function check_realm_cli() {
  if [[ $(type realm-cli 2>&1) =~ "not found" ]]; then
    echo "'realm-cli' is not found. Install 'realm-cli' and try again"
    exit 1
  fi

  return 0
}

function get_hybrid_workshop_repo() {
    local GIT_BRANCH="master"
    local GIT_REPO="confluentinc/confluent-hybrid-cloud-workshop"
    WS_REPO_FOLDER=$DIR_DEMO_HELPER/confluent-hybrid-cloud-workshop
    [[ -d "$WS_REPO_FOLDER" ]] || git clone https://github.com/${GIT_REPO}.git $WS_REPO_FOLDER
    (cd $WS_REPO_FOLDER && git fetch && git checkout ${GIT_BRANCH} && git pull) || {
        echo "ERROR: There seems to be an issue in Downloading $GIT_REPO. Please troubleshoot and try again."
        exit 1
    }

    return 0
}

function get_ccloud_stack() {
    CCLOUD_STACK_URL="${EXAMPLES_URL}/ccloud/ccloud-stack"
    WS_REPO_FOLDER=$DIR_DEMO_HELPER/../ccloud/ccloud_stack
    mkdir -p $WS_REPO_FOLDER
    [ ! -f $WS_REPO_FOLDER/ccloud_stack_destroy.sh ] && wget $CCLOUD_STACK_URL/ccloud_stack_destroy.sh -P $WS_REPO_FOLDER
    [ ! -f $WS_REPO_FOLDER/ccloud_stack_create.sh ] && wget $CCLOUD_STACK_URL/ccloud_stack_create.sh -P $WS_REPO_FOLDER

    return 0
}


