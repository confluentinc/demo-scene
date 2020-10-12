source .env
while [ 1 -eq 1 ];
do
    curl --show-error --silent https://datahub.bradford.gov.uk/ebase/api/getData/v2/Council/CarParkCurrent | \
        tail -n +2 | \
        docker run --rm --interactive edenhill/kafkacat:1.6.0 \
            -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN \
            -X ssl.ca.location=./etc/ssl/cert.pem -X api.version.request=true \
            -b $CCLOUD_BROKER_HOST \
            -X sasl.username="$CCLOUD_API_KEY" \
            -X sasl.password="$CCLOUD_API_SECRET" \
            -t carparks -P -T
    sleep 180
done
