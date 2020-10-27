#!/bin/bash

#########################################
# This script uses real Confluent Cloud resources.
# To avoid unexpected charges, carefully evaluate the cost of resources before launching the script and ensure all resources are destroyed after you are done running it.
#########################################

export CLUSTER_CLOUD=gcp
# Use to find the closest http://www.gcping.com/
export CLUSTER_REGION=us-east4
#CLUSTER_CLOUD=aws

# Source library
source ../common/colors.sh
source ../common/helper.sh
source ./ccloud_library.sh

ccloud::validate_version_ccloud_cli 1.7.0 || exit 1
check_jq || exit 1
ccloud::validate_logged_in_ccloud_cli || exit 1

ccloud::prompt_continue_ccloud_demo || exit 1

enable_ksqldb=false
echo -e "Do you also want to create a Confluent Cloud 🚀  ksqlDB app ${RED}(hourly charges may apply)${NC}? [y/n] "
read -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
  enable_ksqldb=true
fi

echo
ccloud::create_ccloud_stack $enable_ksqldb || exit 1

echo
echo -e "${BLUE}Validating...${NC}"
SERVICE_ACCOUNT_ID=$(ccloud kafka cluster list -o json | jq -r '.[0].name' | awk -F'-' '{print $4;}')
CONFIG_FILE=stack-configs/java-service-account-$SERVICE_ACCOUNT_ID.config
ccloud::validate_ccloud_config $CONFIG_FILE || exit 1
./ccloud-generate-cp-config.sh $CONFIG_FILE > /dev/null
source delta_configs/env.delta

if $enable_ksqldb ; then
  MAX_WAIT=500
  echo -e "${GREEN}Waiting up to $MAX_WAIT seconds for Confluent Cloud ksqlDB cluster to be UP${NC}"
  retry $MAX_WAIT ccloud::validate_ccloud_ksqldb_endpoint_ready $KSQLDB_ENDPOINT || exit 1
fi

ccloud::validate_ccloud_stack_up $CLOUD_KEY $CONFIG_FILE $enable_ksqldb || exit 1

echo
echo "ACLs in this cluster:"
ccloud kafka acl list

echo
echo -e "${GREEN}Local client configuration file written to ${BOLD}${BLUE}${CONFIG_FILE}${NC}"
echo

echo
echo -e "${YELLOW}To destroy this Confluent Cloud stack run ->${NC}"
echo -e "    ./ccloud_stack_destroy.sh $CONFIG_FILE"
echo

echo
ENVIRONMENT=$(ccloud environment list | grep demo-env-$SERVICE_ACCOUNT_ID | tr -d '\*' | awk '{print $1;}')
echo -e "${BLUE}Tip:${NC} 'ccloud' CLI has been set to the new environment ${GREEN}${ENVIRONMENT}${NC}"