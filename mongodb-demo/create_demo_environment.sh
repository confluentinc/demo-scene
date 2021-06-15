#!/bin/bash
set -e

PRJ_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
UTILS_DIR="${PRJ_DIR}/utils"
TMP_FOLDER="${PRJ_DIR}/tmp"
TFS_PATH="${PRJ_DIR}/terraform"
STATE_FILE_PATH="${TFS_PATH}/terraform.tfstate"
WS_STATE_FILE_PATH="${TMP_FOLDER}/.terraform_staging/terraform.tfstate"
CCLOUD_STACK_PATH="${PRJ_DIR}/ccloud/ccloud_stack"
ENV_DELTA_CFG="${CCLOUD_STACK_PATH}/delta_configs/env.delta"
ENV_FILEPATH="${TMP_FOLDER}/envs.created"
CFG_FILE="${PRJ_DIR}/config/demo.cfg"


#http://patorjk.com/software/taag/#p=display&f=ANSI%20Shadow&t=hybrid%20cloud%20%0A%20%20%20%20%20%20demo%0A%20Confluent%20%0A%20%20%20MongoDB
function welcome_screen {
    local DEMO_IP=$VM_HOST
    local DEMO_SITE="http://${DEMO_IP}"
    local C3_LINK="http://${DEMO_IP}:9021"
    local REALM_APP_LINK="https://${REALM_APP_ID}.mongodbstitch.com"

    echo "                                                                                              ";
    echo "██╗  ██╗██╗   ██╗██████╗ ██████╗ ██╗██████╗      ██████╗██╗      ██████╗ ██╗   ██╗██████╗  ";  
    echo "██║  ██║╚██╗ ██╔╝██╔══██╗██╔══██╗██║██╔══██╗    ██╔════╝██║     ██╔═══██╗██║   ██║██╔══██╗ ";  
    echo "███████║ ╚████╔╝ ██████╔╝██████╔╝██║██║  ██║    ██║     ██║     ██║   ██║██║   ██║██║  ██║ ";  
    echo "██╔══██║  ╚██╔╝  ██╔══██╗██╔══██╗██║██║  ██║    ██║     ██║     ██║   ██║██║   ██║██║  ██║ ";  
    echo "██║  ██║   ██║   ██████╔╝██║  ██║██║██████╔╝    ╚██████╗███████╗╚██████╔╝╚██████╔╝██████╔╝ ";  
    echo "╚═╝  ╚═╝   ╚═╝   ╚═════╝ ╚═╝  ╚═╝╚═╝╚═════╝      ╚═════╝╚══════╝ ╚═════╝  ╚═════╝ ╚═════╝  ";  
    echo "                                                                                           ";  
    echo "                        ██████╗ ███████╗███╗   ███╗ ██████╗                                ";  
    echo "                        ██╔══██╗██╔════╝████╗ ████║██╔═══██╗                               ";  
    echo "                        ██║  ██║█████╗  ██╔████╔██║██║   ██║                               ";  
    echo "                        ██║  ██║██╔══╝  ██║╚██╔╝██║██║   ██║                               ";  
    echo "                        ██████╔╝███████╗██║ ╚═╝ ██║╚██████╔╝                               ";  
    echo "                        ╚═════╝ ╚══════╝╚═╝     ╚═╝ ╚═════╝                                ";  
    echo "                                                                                           ";  
    echo "     ██████╗ ██████╗ ███╗   ██╗███████╗██╗     ██╗   ██╗███████╗███╗   ██╗████████╗        ";  
    echo "    ██╔════╝██╔═══██╗████╗  ██║██╔════╝██║     ██║   ██║██╔════╝████╗  ██║╚══██╔══╝        ";  
    echo "    ██║     ██║   ██║██╔██╗ ██║█████╗  ██║     ██║   ██║█████╗  ██╔██╗ ██║   ██║           ";  
    echo "    ██║     ██║   ██║██║╚██╗██║██╔══╝  ██║     ██║   ██║██╔══╝  ██║╚██╗██║   ██║           ";  
    echo "    ╚██████╗╚██████╔╝██║ ╚████║██║     ███████╗╚██████╔╝███████╗██║ ╚████║   ██║           ";  
    echo "     ╚═════╝ ╚═════╝ ╚═╝  ╚═══╝╚═╝     ╚══════╝ ╚═════╝ ╚══════╝╚═╝  ╚═══╝   ╚═╝           ";  
    echo "                                                                                           ";  
    echo "            ███╗   ███╗ ██████╗ ███╗   ██╗ ██████╗  ██████╗ ██████╗ ██████╗                ";  
    echo "            ████╗ ████║██╔═══██╗████╗  ██║██╔════╝ ██╔═══██╗██╔══██╗██╔══██╗               ";  
    echo "            ██╔████╔██║██║   ██║██╔██╗ ██║██║  ███╗██║   ██║██║  ██║██████╔╝               ";  
    echo "            ██║╚██╔╝██║██║   ██║██║╚██╗██║██║   ██║██║   ██║██║  ██║██╔══██╗               ";  
    echo "            ██║ ╚═╝ ██║╚██████╔╝██║ ╚████║╚██████╔╝╚██████╔╝██████╔╝██████╔╝               ";  
    echo "            ╚═╝     ╚═╝ ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝  ╚═════╝ ╚═════╝ ╚═════╝                ";  
    echo "                                                                                           ";  
    echo "                                                                                           ";
    echo "*******************************************************************************************";
    echo " ";
    echo " ";
    echo "Handy links: "
    echo " - External IP: ${DEMO_IP} ";
    echo " - Confluent Control Center: ${C3_LINK}";
    echo " - MongoDB Atlas Realm App: ${REALM_APP_LINK}";
}

function create_tfvars_file {
    cd $PRJ_DIR
    TERRAFORM_CONFIG="$TFS_PATH/config.auto.tfvars"
    echo -e "\n# Create a local configuration file $TERRAFORM_CONFIG with the terraform variables"
    cat <<EOF > $TERRAFORM_CONFIG
ssh_user                = "dc01"
ssh_password            = "$VM_PASSWORD"
vm_host                 = "$VM_HOST"
EOF

}

function init_vars_from_config() {
    if [ ! -f "$CFG_FILE" ]; then
        echo "You are missing the main configuration file! $CFG_FILE does not exist"
        exit 1
    fi

    # Source demo-specific configurations
    source $CFG_FILE

    if [[ -z "$CLOUD_PROVIDER" ]]; then
        echo "You need to specify a demo Type!"
        exit 1
    fi

    ENVIRONMENT_NAME=$DEMO_NAME
    MONGODBATLAS_PROVIDER_NAME=$(echo $CLOUD_PROVIDER | tr 'a-z' 'A-Z')
    MONGODBATLAS_DISK_SIZE_GB="10"
    MONGODBATLAS_MONGO_DB_MAJOR_VERSION="4.0"
    MONGODBATLAS_DBUSER_USERNAME="confluent"
    enable_ksqldb=false
    MONGODBATLAS_DBUSER_PASSWORD=$DEMO_PASSWORD

    VM_PASSWORD=$DEMO_PASSWORD

    case $CLOUD_PROVIDER in
     gcp)
        if [[ -z $GCP_PROJECT || -z $GCP_JSON_CREDENTIALS_PATH || -z $GCP_REGION || -z $GCP_REGION_ZONE ]]; then
            echo "one or more variables are undefined, check your $CFG_FILE "
            echo "required variables: GCP_PROJECT, GCP_JSON_CREDENTIALS_PATH, GCP_REGION, GCP_REGION_ZONE "
        fi
        #GCP_GCS_STORAGE_CLASS=$(jq '.gcp.gcs.storage_class' $CFG_FILE -r)
        #GBQ_PROJECT=$(jq '.gcp.big_query.project' $CFG_FILE -r)
        #GBQ_LOCATION=$(jq '.gcp.big_query.location' $CFG_FILE -r)
        ;;
     azure)
        AZURE_SUBSCRIPTION_ID=$(jq '.azure.subscription_id' $CFG_FILE -r)
        AZURE_CLIENT_ID=$(jq '.azure.client_id' $CFG_FILE -r)
        AZURE_CLIENT_SECRET=$(jq '.azure.client_secret' $CFG_FILE -r)
        AZURE_TENANT_ID=$(jq '.azure.tenant_id' $CFG_FILE -r)
        AZURE_LOCATION=$(jq '.azure.location' $CFG_FILE -r)
        ;; 
    aws)
        AWS_REGION=$(jq '.aws.region' $CFG_FILE -r)
        AWS_ACCESS_KEY=$(jq '.aws.access_key' $CFG_FILE -r)
        AWS_SECRET_KEY=$(jq '.aws.secret_key' $CFG_FILE -r)
        ;; 
     *)
        echo "Use one of the CLOUD_PROVIDER: gcp "
        exit 1
        ;;
    esac

}

function create_ws_config_file (){
    local WORKSHOP_CFG_TEMPLATE="${PRJ_DIR}/templates/ws_configs/workshop-example-${CLOUD_PROVIDER}.yaml"
    cat >$TMP_FOLDER/workshop.yaml <(eval "cat <<EOF
$(<$WORKSHOP_CFG_TEMPLATE)
EOF
")
} 


function create_infrastructure (){

    cd $WS_REPO_FOLDER
    ./workshop-create.py --dir $TMP_FOLDER

    # Read VM IP from workshop terraform output
    VM_HOST=$(terraform output -json -state=${WS_STATE_FILE_PATH} | jq ".external_ip_addresses.value[0]" -r)
    REALM_APP_ID=$(terraform output -json -state=${WS_STATE_FILE_PATH} | jq ".realm_app_id.value" -r)

    # Initialize demo using terraform
    create_tfvars_file
    cd $TFS_PATH
    terraform init
    terraform apply --auto-approve
}

function create_ccloud_resources {
    if [ "$(ls -A $CCLOUD_STACK_PATH/stack-configs/ )" ]
    then
        echo "Files found"
        ls -A $CCLOUD_STACK_PATH/stack-configs/
        echo "There is already an existing Confluent stack, will not recreate"
        return
    fi
    cd $CCLOUD_STACK_PATH
    export CLUSTER_CLOUD=$CLOUD_PROVIDER
    source ccloud_stack_create.sh
}



function start_demo {

    source $UTILS_DIR/demo_helper.sh

    get_ccloud_stack

    check_jq || exit 1
    check_realm_cli || exit 1

    init_vars_from_config

    get_hybrid_workshop_repo
    create_ccloud_resources

    source $ENV_DELTA_CFG

    create_ws_config_file

    # Create the Infrastructure
    create_infrastructure

    welcome_screen
}

mkdir -p logs
start_demo 2>&1 | tee -a logs/demo_creation.log


