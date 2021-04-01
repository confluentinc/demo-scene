#!/bin/bash

source .env

curl -s -L -u "$NROD_USERNAME:$NROD_PASSWORD" "https://datafeeds.networkrail.co.uk/ntrod/CifFileAuthenticate?type=CIF_EA_TOC_FULL_DAILY&day=toc-full" | \
  gunzip | \
  docker run -i \
  edenhill/kafkacat:1.7.0-PRE1 \
    -b $CCLOUD_BROKER_HOST:9092 \
    -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN -X api.version.request=true \
    -X sasl.username=$CCLOUD_API_KEY -X sasl.password=$CCLOUD_API_SECRET \
    -P -t CIF_FULL_DAILY
    
curl -s -L -u "$NROD_USERNAME:$NROD_PASSWORD" "https://datafeeds.networkrail.co.uk/ntrod/CifFileAuthenticate?type=CIF_ED_TOC_FULL_DAILY&day=toc-full" | \
  gunzip | \
  docker run -i \
  edenhill/kafkacat:1.7.0-PRE1 \
    -b $CCLOUD_BROKER_HOST:9092 \
    -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN -X api.version.request=true \
    -X sasl.username=$CCLOUD_API_KEY -X sasl.password=$CCLOUD_API_SECRET \
    -P -t CIF_FULL_DAILY

curl -s -L -u "$NROD_USERNAME:$NROD_PASSWORD" "https://datafeeds.networkrail.co.uk/ntrod/CifFileAuthenticate?type=CIF_HB_TOC_FULL_DAILY&day=toc-full" | \
  gunzip | \
  docker run -i \
  edenhill/kafkacat:1.7.0-PRE1 \
    -b $CCLOUD_BROKER_HOST:9092 \
    -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN -X api.version.request=true \
    -X sasl.username=$CCLOUD_API_KEY -X sasl.password=$CCLOUD_API_SECRET \
    -P -t CIF_FULL_DAILY

curl -s -L -u "$NROD_USERNAME:$NROD_PASSWORD" "https://datafeeds.networkrail.co.uk/ntrod/CifFileAuthenticate?type=CIF_EM_TOC_FULL_DAILY&day=toc-full" | \
  gunzip | \
  docker run -i \
  edenhill/kafkacat:1.7.0-PRE1 \
    -b $CCLOUD_BROKER_HOST:9092 \
    -X security.protocol=SASL_SSL -X sasl.mechanisms=PLAIN -X api.version.request=true \
    -X sasl.username=$CCLOUD_API_KEY -X sasl.password=$CCLOUD_API_SECRET \
    -P -t CIF_FULL_DAILY