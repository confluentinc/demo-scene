#!/bin/bash
echo -e "\n\n--\n[CCloud] Latest message on raw ingest:\n\t"
kafkacat -b <CCLOUD_BROKER> \
         -X 'security.protocol=SASL_SSL' -X 'sasl.mechanisms=PLAIN' -X 'api.version.request=true' \
         -X 'sasl.username=<CCLOUD_API_KEY>' \
         -X 'sasl.password=<CCLOUD_API_SECRET>' \
         -C -c1 -o-1 -t networkrail_TRAIN_MVT_v11 | jq '.timestamp' | sed -e 's/"//g' | sed -e 's/...$//g' | xargs -Ifoo date -j -f %s foo

echo -e "\n[CCloud] Latest message from exploded batch:\n\t"
kafkacat -b <CCLOUD_BROKER> \
         -X 'security.protocol=SASL_SSL' -X 'sasl.mechanisms=PLAIN' -X 'api.version.request=true' \
         -X 'sasl.username=<CCLOUD_API_KEY>' \
         -X 'sasl.password=<CCLOUD_API_SECRET>' \
         -C -c1 -o-1 -t networkrail_TRAIN_MVT_X_v03 | jq '.header.msg_queue_timestamp' | sed -e 's/"//g' | sed -e 's/000$//g' | xargs -Ifoo date -j -f %s foo         