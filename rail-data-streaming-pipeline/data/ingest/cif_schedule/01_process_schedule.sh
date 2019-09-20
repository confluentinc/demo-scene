#!/usr/bin/env bash
# This extracts the final element of the array into a route element

#kafkacat -b localhost:9092 -e -t CIF_FULL_DAILY -o beginning | \
kafkacat -b localhost:9092 -e -u -G 00_ingest_schedule.sh CIF_FULL_DAILY | \
  jq -c '.|select(.JsonScheduleV1) | .JsonScheduleV1 + {last_schedule_segment:.JsonScheduleV1.schedule_segment.schedule_location[-1:][]}' | \
  kafkacat -b localhost:9092 -t JsonScheduleV1 -P
