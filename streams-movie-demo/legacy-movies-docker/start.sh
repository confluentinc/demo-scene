#!/bin/bash

RED='\033[0;31m'
NC='\033[0m' # No Color
GREEN='\033[0;32m'

# Source library
. ./scripts/helper.sh

check_jq || exit

export CONFIG_FILE=~/.ccloud/config
export USE_CONFLUENT_CLOUD_SCHEMA_REGISTRY=true
export USE_CONFLUENT_CLOUD_KSQL=true

check_ccloud_config $CONFIG_FILE || exit

if [[ "${USE_CONFLUENT_CLOUD_SCHEMA_REGISTRY}" == true ]]; then
  SCHEMA_REGISTRY_CONFIG_FILE=$HOME/.ccloud/config
else
  SCHEMA_REGISTRY_CONFIG_FILE=schema_registry_docker.config
fi
./scripts/ccloud-generate-cp-configs.sh $CONFIG_FILE $SCHEMA_REGISTRY_CONFIG_FILE

DELTA_CONFIGS_DIR=delta_configs
source $DELTA_CONFIGS_DIR/env.delta

if [[ "$USE_CONFLUENT_CLOUD_SCHEMA_REGISTRY" == true ]]; then
  validate_confluent_cloud_schema_registry $SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO $SCHEMA_REGISTRY_URL || exit 1
fi

docker-compose up -d

echo "Confluent Platform is starting up..."

# ---- Set up Replicator source connector ---
export CONNECT_HOST=connect-cloud
echo -e "\n--\n\n$(date) Waiting for Kafka Connect to start on ${GREEN}$CONNECT_HOST ${NC}… ⏳"
grep -q "Kafka Connect started" <(docker-compose logs -f $CONNECT_HOST)
echo -e "\n--\n$(date) 👉 Creating Replicator to CCloud connector"
#. ./scripts/submit_replicator_docker_config.sh


# ---- Set up Debezium source connector ---
export CONNECT_HOST=connect-debezium
echo -e "\n--\n\n$(date) Waiting for Kafka Connect to start on ${GREEN}$CONNECT_HOST${NC}… ⏳"
grep -q "Kafka Connect started" <(docker-compose logs -f $CONNECT_HOST)
echo -e "\n--\n$(date) 👉 Creating Debezium connector"
#. ./scripts/submit_debezium_config.sh

# Reregister a schema for a topic with a different name
#curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data "{\"schema\": $(curl -s http://localhost:8085/subjects/pageviews-value/versions/latest | jq '.schema')}" http://localhost:8085/subjects/pageviews.replica-value/versions
