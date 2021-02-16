#!/bin/bash
source .env
while [ 1 -eq 1 ];
do
http -a $LEEDS_USER:$LEEDS_PW get http://www.leedstravel.info/datex2/carparks/content.xml | \
        xq -c '."d2lm:d2LogicalModel"."d2lm:payloadPublication"."d2lm:situation"[]."d2lm:situationRecord"' | \
docker run --rm --interactive edenhill/kafkacat:1.6.0 \
            -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN \
            -X ssl.ca.location=./etc/ssl/cert.pem -X api.version.request=true \
            -b $CCLOUD_BROKER_HOST \
            -X sasl.username="$CCLOUD_API_KEY" \
            -X sasl.password="$CCLOUD_API_SECRET" \
            -t carparks_leeds -P -T
    sleep 60
done
