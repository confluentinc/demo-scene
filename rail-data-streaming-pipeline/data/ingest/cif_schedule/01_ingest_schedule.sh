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

# NB above will throw `jq: error (at <stdin>:30): Cannot iterate over null (null)` for records like this: 
# {
#   "JsonScheduleV1": {
#     "CIF_bank_holiday_running": null,
#     "CIF_stp_indicator": "C",
#     "CIF_train_uid": "Y64223",
#     "schedule_days_runs": "0000010",
#     "schedule_end_date": "2019-12-14",
#     "schedule_segment": {
#       "signalling_id": "    ",
#       "CIF_train_category": "",
#       "CIF_headcode": "",
#       "CIF_course_indicator": 1,
#       "CIF_train_service_code": "        ",
#       "CIF_business_sector": "??",
#       "CIF_power_type": null,
#       "CIF_timing_load": null,
#       "CIF_speed": null,
#       "CIF_operating_characteristics": null,
#       "CIF_train_class": null,
#       "CIF_sleepers": null,
#       "CIF_reservations": null,
#       "CIF_connection_indicator": null,
#       "CIF_catering_code": null,
#       "CIF_service_branding": ""
#     },
#     "schedule_start_date": "2019-12-14",
#     "train_status": " ",
#     "transaction_type": "Create"
#   }
# }

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
