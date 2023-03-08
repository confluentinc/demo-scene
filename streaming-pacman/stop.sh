#!/bin/bash
# Source library 

PRJ_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
UTILS_DIR="${PRJ_DIR}/utils"
TFS_PATH="${PRJ_DIR}/terraform/aws"
export EXAMPLE="streaming-pacman"

LOGS_FOLDER="${PRJ_DIR}/logs"
LOG_FILE_PATH="${LOGS_FOLDER}/stop.log"


function end_demo {
  
    # Source library
    source $UTILS_DIR/demo_helper.sh 

    # Source demo-specific configurations
    source $PRJ_DIR/config/demo.cfg

    # # Destroy Confluent Cloud resources
    # for STACK_FILE in $(ls $PRJ_DIR/stack-configs); 
    # do 
    #     DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
    #     CONFIG_FILE=$PRJ_DIR/stack-configs/$STACK_FILE
    #     ccloud::ccloud_stack_destroy $CONFIG_FILE
    #     rm $CONFIG_FILE
    # done

    # echo "Removing folder: $PRJ_DIR/delta_configs"
    # rm -r $PRJ_DIR/delta_configs

    # Destroy Demo Infrastructure using Terraform
    cd $TFS_PATH
    terraform destroy --auto-approve

    #rm -f "${TFS_PATH}/config.auto.tfvars"
    
    #rm -f "${TMP_FOLDER}/cluster_1.client.config"
    #rm -f "${TMP_FOLDER}/cluster_2.client.config"
    #rm -f "${TMP_FOLDER}/cluster_3.client.config"

}

mkdir $LOGS_FOLDER
end_demo $1 2>&1 | tee -a $LOG_FILE_PATH