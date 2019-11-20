#!/usr/bin/env bash
# @rmoff / June 11, 2019

echo '----'
# Set the path so cron can find jq
export PATH=$PATH:/usr/local/bin/

# What time is it Mr Wolf? 
date 

# List current connectors and status
curl -s "http://localhost:28083/connectors"| jq '.[]'| xargs -I{connector_name} curl -s "http://localhost:28083/connectors/"{connector_name}"/status"| jq -c -M '[.name,.connector.state,.tasks[].state]|join(":|:")'| column -s : -t| sed 's/\"//g'| sort

# Delete any Elasticsearch sink connectors
curl -s "http://localhost:28083/connectors" | \
  jq -c '.[] | select(. | startswith("sink-elastic")) ' | \
  sed -e 's/\"//g'| \
  xargs -I{name}  curl -X DELETE "http://localhost:28083/connectors/"{name}

# What time is it Mr Wolf? 
date 

# List current connectors and status
curl -s "http://localhost:28083/connectors"| jq '.[]'| xargs -I{connector_name} curl -s "http://localhost:28083/connectors/"{connector_name}"/status"| jq -c -M '[.name,.connector.state,.tasks[].state]|join(":|:")'| column -s : -t| sed 's/\"//g'| sort

