#!/usr/bin/env bash

echo "Opting out of telemetry"
curl 'http://localhost:5601/api/telemetry/v2/optIn' \
  -H 'kbn-xsrf: nevergonnagiveyouup' \
  -H 'Content-Type: application/json' \
  -d '{"enabled":false}' 

curl 'http://localhost:5601/api/license/start_trial' \
  -X 'POST' \
  -H 'kbn-version: 7.11.0' \
  -H 'Content-Type: application/json' 