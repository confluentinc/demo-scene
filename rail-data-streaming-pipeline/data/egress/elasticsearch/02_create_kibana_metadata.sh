#!/usr/bin/env bash

echo "Create index patterns"
curl -XPOST 'http://localhost:5601/api/saved_objects/index-pattern/train_cancellations_activations_schedule_00' \
    -H 'kbn-xsrf: nevergonnagiveyouup' \
    -H 'Content-Type: application/json' \
    -d '{"attributes":{"title":"train_cancellations_activations_schedule_00","timeFieldName":"CANX_TIMESTAMP"}}'

curl -XPOST 'http://localhost:5601/api/saved_objects/index-pattern/train_movements_activations_schedule_00' \
    -H 'kbn-xsrf: nevergonnagiveyouup' \
    -H 'Content-Type: application/json' \
    -d '{"attributes":{"title":"train_movements_activations_schedule_00","timeFieldName":"ACTUAL_TIMESTAMP"}}'

curl -XPOST 'http://localhost:5601/api/saved_objects/index-pattern/train_cancellations_00' \
    -H 'kbn-xsrf: nevergonnagiveyouup' \
    -H 'Content-Type: application/json' \
    -d '{"attributes":{"title":"train_cancellations_00","timeFieldName":"CANX_TIMESTAMP"}}'

curl -XPOST 'http://localhost:5601/api/saved_objects/index-pattern/train_movements_00' \
    -H 'kbn-xsrf: nevergonnagiveyouup' \
    -H 'Content-Type: application/json' \
    -d '{"attributes":{"title":"train_movements_00","timeFieldName":"ACTUAL_TIMESTAMP"}}'

curl -XPOST 'http://localhost:5601/api/saved_objects/index-pattern/schedule_00' \
    -H 'kbn-xsrf: nevergonnagiveyouup' \
    -H 'Content-Type: application/json' \
    -d '{"attributes":{"title":"train-schedule_00"}}'

echo "Setting the index pattern as default"
curl -XPOST 'http://localhost:5601/api/kibana/settings' \
    -H 'kbn-xsrf: nevergonnagiveyouup' \
    -H 'content-type: application/json' \
    -d '{"changes":{"defaultIndex":"train_movements_activations_schedule_00"}}'

echo "Opting out of telemetry"
curl 'http://localhost:5601/api/telemetry/v2/optIn' \
  -H 'kbn-xsrf: nevergonnagiveyouup' \
  -H 'Content-Type: application/json' \
  -d '{"enabled":false}' 
