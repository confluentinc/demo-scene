#!/bin/bash

# Bring up Docker Compose
echo -e "Bringing up Docker Compose"
docker-compose -p twitter-streams up -d

echo -e "Waiting for Control Center to start"
# Verify Confluent Control Center has started within 120 seconds
MAX_WAIT=120
CUR_WAIT=0
while [[ ! $(docker-compose logs control-center) =~ "Started NetworkTrafficServerConnector" ]]; do
  sleep 10
  CUR_WAIT=$(( CUR_WAIT+10 ))
  if [[ "$CUR_WAIT" -gt "$MAX_WAIT" ]]; then
    echo -e "\nERROR: The logs in control-center container do not show 'Started NetworkTrafficServerConnector'. Please troubleshoot with 'docker-compose ps' and 'docker-compose logs'.\n"
    exit 1
  fi
done

./scripts/01-start-twitter-connector.sh
./scripts/03-rename-cluster.sh