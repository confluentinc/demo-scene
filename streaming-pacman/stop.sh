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

    # Destroy Demo Infrastructure using Terraform
    cd $TFS_PATH
    terraform destroy --auto-approve

}

mkdir $LOGS_FOLDER
end_demo $1 2>&1 | tee -a $LOG_FILE_PATH