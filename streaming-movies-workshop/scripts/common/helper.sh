function check_jq() {
  if [[ $(type jq 2>&1) =~ "not found" ]]; then
    echo "'jq' is not found. Install 'jq' and try again"
    exit 1
  fi

  return 0
}

function check_ccloud_config() {
  expected_configfile=$1

  if [[ ! -f "$expected_configfile" ]]; then
    echo "Confluent Cloud configuration file does not exist at $expected_configfile. Please create the configuration file with properties set to your Confluent Cloud cluster and try again."
    exit 1
  elif ! [[ $(grep "^\s*bootstrap.server" $expected_configfile) ]]; then
    echo "Missing 'bootstrap.server' in $expected_configfile. Please modify the configuration file with properties set to your Confluent Cloud cluster and try again."
    exit 1
  fi

  return 0
}

function validate_confluent_cloud_schema_registry() {
  auth=$1
  sr_endpoint=$2

  curl --silent -u $auth $sr_endpoint
  if [[ "$?" -ne 0 ]]; then
    echo "ERROR: Could not validate credentials to Confluent Cloud Schema Registry. Please troubleshoot"
    exit 1
  fi
  return 0
}

function check_docker() {
  if ! docker ps -q &>/dev/null; then
    echo "This demo requires Docker but it doesn't appear to be running.  Please start Docker and try again."
    exit 1
  fi

  return 0
}


retry() {
    local -r -i max_wait="$1"; shift
    local -r cmd="$@"

    local -i sleep_interval=5
    local -i curr_wait=0

    until $cmd
    do
        if (( curr_wait >= max_wait ))
        then
            echo "ERROR: Failed after $curr_wait seconds. Please troubleshoot and run again."
            return 1
        else
            printf "."
            curr_wait=$((curr_wait+sleep_interval))
            sleep $sleep_interval
        fi
    done
    printf "\n"
}