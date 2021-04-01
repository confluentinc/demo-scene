#!/usr/bin/env bash
# @rmoff / June 6, 2019

echo -e '\n----'

# What time is it Mr Wolf? 
date 

# List current connectors and status
echo -e "\nCurrent state: "
curl -s "http://cloud_kc_worker:8083/connectors"| jq '.[]'| xargs -I{connector_name} curl -s "http://cloud_kc_worker:8083/connectors/"{connector_name}"/status"| jq -c -M '[.name,.connector.state,.tasks[].state]|join(":|:")'| column -s : -t| sed 's/\"//g'| sort

# Restart any connector tasks that are FAILED
echo -e "\nRestarting any failed connectors…"
curl -s "http://cloud_kc_worker:8083/connectors" | \
  jq '.[]' | \
  xargs -I{connector_name} curl -s "http://cloud_kc_worker:8083/connectors/"{connector_name}"/status" | \
  jq -c -M '[select(.tasks[].state=="FAILED") | .name,"§±§",.tasks[].id]' | \
  grep -v "\[\]"| \
  sed -e 's/^\[\"//g'| sed -e 's/\",\"§±§\",/\/tasks\//g'|sed -e 's/\]$//g'| \
  xargs -I{connector_and_task} curl -X POST "http://cloud_kc_worker:8083/connectors/"{connector_and_task}"/restart"

# Wait for them to restart
echo -e "\nWaiting for a minute"
sleep 60

# List current connectors and status
echo -e "\nStatus now:"
curl -s "http://cloud_kc_worker:8083/connectors"| jq '.[]'| xargs -I{connector_name} curl -s "http://cloud_kc_worker:8083/connectors/"{connector_name}"/status"| jq -c -M '[.name,.connector.state,.tasks[].state]|join(":|:")'| column -s : -t| sed 's/\"//g'| sort

