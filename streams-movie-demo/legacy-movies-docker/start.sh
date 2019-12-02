#!/bin/bash

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
./ccloud-generate-cp-configs.sh $CONFIG_FILE $SCHEMA_REGISTRY_CONFIG_FILE

DELTA_CONFIGS_DIR=delta_configs
source $DELTA_CONFIGS_DIR/env.delta

if [[ "$USE_CONFLUENT_CLOUD_SCHEMA_REGISTRY" == true ]]; then
  validate_confluent_cloud_schema_registry $SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO $SCHEMA_REGISTRY_URL || exit 1
fi

docker-compose up -d

echo "Sleeping 90 seconds to wait for all services to come up"
sleep 90

# Reregister a schema for a topic with a different name
#curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data "{\"schema\": $(curl -s http://localhost:8085/subjects/pageviews-value/versions/latest | jq '.schema')}" http://localhost:8085/subjects/pageviews.replica-value/versions

# Replicator
. ./scripts/submit_replicator_docker_config.sh

sleep 30
