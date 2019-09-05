echo "Create index patterns"
curl -XPOST 'http://localhost:5601/api/saved_objects/index-pattern/train_cancellations_activations_schedule_00' \
    -H 'kbn-xsrf: nevergonnagiveyouup' \
    -H 'Content-Type: application/json' \
    -d '{"attributes":{"title":"train_cancellations_activations_schedule_00","timeFieldName":"CREATE_TS"}}'

curl -XPOST 'http://localhost:5601/api/saved_objects/index-pattern/train_movements_activations_schedule_00' \
    -H 'kbn-xsrf: nevergonnagiveyouup' \
    -H 'Content-Type: application/json' \
    -d '{"attributes":{"title":"train_movements_activations_schedule_00","timeFieldName":"CREATE_TS"}}'

curl -XPOST 'http://localhost:5601/api/saved_objects/index-pattern/train_cancellations_02' \
    -H 'kbn-xsrf: nevergonnagiveyouup' \
    -H 'Content-Type: application/json' \
    -d '{"attributes":{"title":"train_cancellations_02","timeFieldName":"CREATE_TS"}}'

curl -XPOST 'http://localhost:5601/api/saved_objects/index-pattern/train_movements_01' \
    -H 'kbn-xsrf: nevergonnagiveyouup' \
    -H 'Content-Type: application/json' \
    -d '{"attributes":{"title":"train_movements_01","timeFieldName":"CREATE_TS"}}'

echo "Setting the index pattern as default"
curl -XPOST 'http://localhost:5601/api/kibana/settings' \
    -H 'kbn-xsrf: nevergonnagiveyouup' \
    -H 'content-type: application/json' \
    -d '{"changes":{"defaultIndex":"train_movements_activations_schedule_00"}}'

echo "Opting out of telemetry"
curl -XPOST 'http://localhost:5601/api/telemetry/v1/optIn' \
    -H 'kbn-xsrf: nevergonnagiveyouup' \
    -H 'content-type: application/json' \
    -d '{"enabled":false}'

