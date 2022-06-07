#!/bin/bash
#set -e

PRJ_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
UTILS_DIR="${PRJ_DIR}/utils"
TMP_FOLDER="${PRJ_DIR}/tmp"
TFS_PATH="${PRJ_DIR}/terraform/aws"
STATE_FILE_PATH="${TFS_PATH}/terraform.tfstate"
LOGS_FOLDER="${PRJ_DIR}/logs"
LOG_FILE_PATH="${LOGS_FOLDER}/start.log"

export EXAMPLE="streaming-pacman"
export TOPICS_TO_CREATE="USER_GAME USER_LOSSES"



function create_tfvars_file {

    AWS_REGION=$(echo "$BOOTSTRAP_SERVERS" | awk -F'.' '{print $2}')
   
    TFVAR_S3_BUCKET=""
    if [ -z ${S3_BUCKET_NAME+x} ]; 
    then 
        echo "S3_BUCKET_NAME is unset"
    else 
        echo "S3_BUCKET_NAME is set to '$S3_BUCKET_NAME'"
        TFVAR_S3_BUCKET="bucket_name=\"${S3_BUCKET_NAME}\"" 
    fi

    cd $PRJ_DIR
    TERRAFORM_CONFIG="$TFS_PATH/configs.auto.tfvars"
    echo -e "\n# Create a local configuration file $TERRAFORM_CONFIG with the terraform variables"
    cat <<EOF > $TERRAFORM_CONFIG
bootstrap_server="$BOOTSTRAP_SERVERS"
cluster_api_key="$CLOUD_KEY"
cluster_api_secret="$CLOUD_SECRET"
ksql_endpoint="$KSQLDB_ENDPOINT"
ksql_basic_auth_user_info="$KSQLDB_BASIC_AUTH_USER_INFO"
aws_access_key="$AWS_ACCESS_KEY"
aws_secret_key="$AWS_SECRET_KEY"
aws_region="$AWS_REGION"
$TFVAR_S3_BUCKET

EOF

}


function create_infra_with_tf (){

    DELTA_CONFIGS_DIR=delta_configs
    source $DELTA_CONFIGS_DIR/env.delta
    
    create_tfvars_file
    cd $TFS_PATH
    terraform init
    terraform apply --auto-approve

}


function create_ccloud_resources {

    if [ "$(ls -A $PRJ_DIR/stack-configs/ )" ]
    then
        echo "Files found"
        ls -A $PRJ_DIR/stack-configs/
        echo "There is already an existing Confluent stack, will not recreate"
        return
    fi

    ccloud::validate_version_cli 1.7.0 \
        && print_pass "confluent CLI version ok" \
        || exit 1

    ccloud::validate_logged_in_cli \
        && print_pass "logged into confluent CLI" \
        || exit 1

    check_jq \
        && print_pass "jq found" \
        || exit 1

    echo
    echo ====== Create new Confluent Cloud stack
    ccloud::prompt_continue_ccloud_demo || exit 1
    ccloud::create_ccloud_stack true
    SERVICE_ACCOUNT_ID=$(confluent kafka cluster describe -o json | jq -r '.name' | awk -F'-' '{print $4 "-" $5;}')
    if [[ "$SERVICE_ACCOUNT_ID" == "" ]]; then
    echo "ERROR: Could not determine SERVICE_ACCOUNT_ID from 'ccloud kafka cluster list'. Please troubleshoot, destroy stack, and try again to create the stack."
    exit 1
    fi
    CONFIG_FILE=stack-configs/java-service-account-$SERVICE_ACCOUNT_ID.config
    export CONFIG_FILE=$CONFIG_FILE
    ccloud::validate_ccloud_config $CONFIG_FILE \
    && print_pass "$CONFIG_FILE ok" \
    || exit 1

    echo ====== Generate CCloud configurations
    ccloud::generate_configs $CONFIG_FILE

    DELTA_CONFIGS_DIR=delta_configs
    source $DELTA_CONFIGS_DIR/env.delta
    printf "\n"

    # Pre-flight check of Confluent Cloud credentials specified in $CONFIG_FILE
    MAX_WAIT=720
    echo "Waiting up to $MAX_WAIT seconds for Confluent Cloud ksqlDB cluster to be UP"
    retry $MAX_WAIT ccloud::validate_ccloud_ksqldb_endpoint_ready $KSQLDB_ENDPOINT || exit 1
    ccloud::validate_ccloud_stack_up $CLOUD_KEY $CONFIG_FILE || exit 1

    # Set Kafka cluster
    ccloud::set_kafka_cluster_use_from_api_key $CLOUD_KEY || exit 1

    #################################################################
    # Confluent Cloud ksqlDB application
    #################################################################
    ./create_ksqldb_app.sh || exit 1


    printf "\nDONE! Connect to your Confluent Cloud UI at https://confluent.cloud/\n"
    echo
    echo "Local client configuration file written to $CONFIG_FILE"
    echo
    echo "Cloud resources are provisioned and accruing charges. To destroy this demo and associated resources run ->"
    echo "    ./stop.sh $CONFIG_FILE"
    echo
    


}

function start_demo {

    # Source demo-specific configurations
    source $PRJ_DIR/config/demo.cfg

    source $UTILS_DIR/demo_helper.sh   

    create_ccloud_resources

    create_infra_with_tf

}

mkdir $LOGS_FOLDER
start_demo 2>&1 | tee -a $LOG_FILE_PATH