#!/bin/bash

echo -e "\n--\n+> Create Kibana index patterns"
curl -XPOST 'http://localhost:5601/api/saved_objects/index-pattern/runner_location' \
    -H 'kbn-xsrf: nevergonnagiveyouup' \
    -H 'Content-Type: application/json' \
    -d '{"attributes":{"title":"runner_location*","timeFieldName":"EVENT_TIME_EPOCH_MS_TS"}}'

echo -e "\n--\n+> Set default Kibana index"
curl -XPOST 'http://localhost:5601/api/kibana/settings' \
    -H 'kbn-xsrf: nevergonnagiveyouup' \
    -H 'content-type: application/json' \
    -d '{"changes":{"defaultIndex":"runner_location"}}'