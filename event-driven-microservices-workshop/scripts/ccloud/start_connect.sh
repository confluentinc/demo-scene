#!/bin/bash

# Source library
. ../common/colors.sh
. ../common/helper.sh

check_jq || exit
# using CP 5.5.1
export CONFLUENT=5.5.1

#export CONFIG_FILE=~/.ccloud/config

if [ -z "$1" ]; then
  echo -e "${RED}ERROR: Must supply argument that is the client configuration file created from './ccloud_stack_create.sh'. (Is it in stack-configs/ folder?) ${NC}"
  exit 1
else
  CONFIG_FILE=$1
fi

export USE_CONFLUENT_CLOUD_SCHEMA_REGISTRY=true
export USE_CONFLUENT_CLOUD_KSQL=true

check_ccloud_config $CONFIG_FILE || exit

if [[ "${USE_CONFLUENT_CLOUD_SCHEMA_REGISTRY}" == true ]]; then
  SCHEMA_REGISTRY_CONFIG_FILE=$CONFIG_FILE
else
  SCHEMA_REGISTRY_CONFIG_FILE=schema_registry_docker.config
fi
./ccloud-generate-cp-config.sh $CONFIG_FILE $SCHEMA_REGISTRY_CONFIG_FILE

DELTA_CONFIGS_DIR=delta_configs
source $DELTA_CONFIGS_DIR/env.delta

if [[ "$USE_CONFLUENT_CLOUD_SCHEMA_REGISTRY" == true ]]; then
  validate_confluent_cloud_schema_registry $SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO $SCHEMA_REGISTRY_URL || exit 1
fi

docker-compose up -d

echo -e "${BLUE}☁️  ${BOLD}Kafka Connect${NC} is starting up...${NC}"

# ---- Set up Replicator source connector ---
export CONNECT_HOST=connect-cloud
echo -e "\n--\n\n$(date) Waiting for Kafka Connect to start on ${GREEN}$CONNECT_HOST ${NC}… ⏳"
grep -q "Kafka Connect started" <(docker-compose logs -f $CONNECT_HOST)
echo -e "\n--\n$(date) 🛢  Creating JDBC to CCloud connector"
#. ./scripts/submit_replicator_docker_config.sh