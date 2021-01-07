#!/usr/bin/env bash
# This extracts the final element of the array into a route element

docker exec kafkacat sh -c " \
  kafkacat -b broker:29092 -e -u -o beginning -G 00_ingest_schedule.sh CIF_FULL_DAILY | \
  jq -c '.|select(.JsonScheduleV1) | .JsonScheduleV1 + {last_schedule_segment:.JsonScheduleV1.schedule_segment.schedule_location[-1:][]}' | \
  kafkacat -b broker:29092 -t JsonScheduleV1 -P"
