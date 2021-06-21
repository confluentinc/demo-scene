#!/bin/bash
set -e

PRJ_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
UTILS_DIR="${PRJ_DIR}/utils"
TFS_PATH="${PRJ_DIR}/terraform"
TMP_FOLDER="$PRJ_DIR/tmp"
ENV_FILEPATH="${TMP_FOLDER}/envs.created"
CCLOUD_STACK_PATH="${PRJ_DIR}/ccloud/ccloud_stack"
STACK_FILEPATH="${TMP_FOLDER}/stack.created"
CFG_FILE="${PRJ_DIR}/config/demo.cfg"


function destroy_ccloud_resources (){
 # Destroy Confluent Cloud resources
    export PRESERVE_ENVIRONMENT=false
    #ENVIRONMENT_NAME=$DEMO_NAME
    ENVIRONMENT_NAME_PREFIX=$DEMO_NAME
    echo "This process will also delete Environment: $DEMO_NAME"
    export QUIET=false
    cd $CCLOUD_STACK_PATH
    # Destroy Confluent Cloud resources
    for STACK_FILE in $(ls $CCLOUD_STACK_PATH/stack-configs); 
    do 
        DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
        CONFIG_FILE=$CCLOUD_STACK_PATH/stack-configs/$STACK_FILE
        source ./ccloud_stack_destroy.sh $CONFIG_FILE
        #rm $CONFIG_FILE
    done

    #echo "Removing folder: $PRJ_DIR/delta_configs"
    #rm -r $PRJ_DIR/delta_configs
    
}

function destroy_infrastructure (){

    cd $WS_REPO_FOLDER
    ./workshop-destroy.py --dir $TMP_FOLDER

    rm $TMP_FOLDER/workshop.yaml

    cd $TFS_PATH
    terraform destroy --auto-approve

    rm -f "${TFS_PATH}/config.auto.tfvars"

}

function end_demo {
  
  # Source library
  source $UTILS_DIR/demo_helper.sh 

  # Source demo-specific configurations
  source $CFG_FILE
  
  check_jq || exit 1

  destroy_ccloud_resources

  get_hybrid_workshop_repo
  destroy_infrastructure  
 
  #rm -f "${TMP_FOLDER}/cluster_1.client.config"

}

mkdir -p logs
end_demo 2>&1 | tee -a logs/demo_destruction.log