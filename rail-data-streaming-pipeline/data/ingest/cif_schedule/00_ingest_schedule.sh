#!/usr/bin/env bash

export PATH=$PATH:/home/rmoff/kafkacat/
source $(dirname $(readlink -f $0))/../../set_credentials_env.sh

# Make sure that on the SCHEDULE feeds page at 
# https://datafeeds.networkrail.co.uk/ntrod/myFeeds you are subscribed
# to the appropriate feed for the Train Operating Company (TOC) for 
# which you are pulling the schedule data
curl -s -L -u "$NROD_USERNAME:$NROD_PASSWORD" "https://datafeeds.networkrail.co.uk/ntrod/CifFileAuthenticate?type=CIF_EA_TOC_FULL_DAILY&day=toc-full" | \
  gunzip | \
  kafkacat -b localhost -P -t CIF_FULL_DAILY

curl -s -L -u "$NROD_USERNAME:$NROD_PASSWORD" "https://datafeeds.networkrail.co.uk/ntrod/CifFileAuthenticate?type=CIF_ED_TOC_FULL_DAILY&day=toc-full" | \
  gunzip | \
  kafkacat -b localhost -P -t CIF_FULL_DAILY

curl -s -L -u "$NROD_USERNAME:$NROD_PASSWORD" "https://datafeeds.networkrail.co.uk/ntrod/CifFileAuthenticate?type=CIF_HB_TOC_FULL_DAILY&day=toc-full" | \
  gunzip | \
  kafkacat -b localhost -P -t CIF_FULL_DAILY

curl -s -L -u "$NROD_USERNAME:$NROD_PASSWORD" "https://datafeeds.networkrail.co.uk/ntrod/CifFileAuthenticate?type=CIF_EM_TOC_FULL_DAILY&day=toc-full" | \
  gunzip | \
  kafkacat -b localhost -P -t CIF_FULL_DAILY

# curl -s -L -u "$NROD_USERNAME:$NROD_PASSWORD" "https://datafeeds.networkrail.co.uk/ntrod/CifFileAuthenticate?type=CIF_EA_TOC_FULL_DAILY&day=toc-full" | \
#   gunzip >  CIF_FULL_DAILY.json
# curl -s -L -u "$NROD_USERNAME:$NROD_PASSWORD" "https://datafeeds.networkrail.co.uk/ntrod/CifFileAuthenticate?type=CIF_ED_TOC_FULL_DAILY&day=toc-full" | \
#   gunzip >>  CIF_FULL_DAILY.json
# curl -s -L -u "$NROD_USERNAME:$NROD_PASSWORD" "https://datafeeds.networkrail.co.uk/ntrod/CifFileAuthenticate?type=CIF_HB_TOC_FULL_DAILY&day=toc-full" | \
#   gunzip >>  CIF_FULL_DAILY.json
# curl -s -L -u "$NROD_USERNAME:$NROD_PASSWORD" "https://datafeeds.networkrail.co.uk/ntrod/CifFileAuthenticate?type=CIF_EM_TOC_FULL_DAILY&day=toc-full" | \
#   gunzip >>  CIF_FULL_DAILY.json


## Rejected option : filter records at ingest time, splitting into separate topics: 
## Why? Better to keep the logic in KSQL if possible.
# curl -s -L -u "NROD_USERNAME:NROD_PASSWORD" "https://datafeeds.networkrail.co.uk/ntrod/CifFileAuthenticate?type=CIF_EA_TOC_FULL_DAILY&day=toc-full" | \
#   gunzip | \
#   grep JsonScheduleV1 | \
#   kafkacat -b localhost -P -t CIF_EA_TOC_FULL_DAILY_JsonScheduleV1

# curl -s -L -u "NROD_USERNAME:NROD_PASSWORD" "https://datafeeds.networkrail.co.uk/ntrod/CifFileAuthenticate?type=CIF_EA_TOC_FULL_DAILY&day=toc-full" | \
#   gunzip | \
#   grep TiplocV1 | \
#   kafkacat -b localhost -P -t CIF_EA_TOC_FULL_DAILY_TiplocV1
