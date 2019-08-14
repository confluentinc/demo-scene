#!/usr/bin/env bash

# The train movement data is sent in batches of up to 32 messages
# The format is as JSON within the text field of the message
#
# $ kafkacat -b localhost:9092 -t networkrail_TRAIN_MVT -o end -C
# {"messageID":"ID:opendata-backend.rockshore.net-38887-1556015157154-11:11706:3:37:29205","messageType":"text","timestamp":1558563175499,"deliveryMode":2,"correlationID":null,"replyTo":null,"destination":{"destinationType":"topic","name":"TRAIN_MVT_EA_TOC"},"redelivered":false,"type":null,"expiration":1558563475499,"priority":4,"properties":{},"bytes":null,"map":null,"text":"[{\"header\":{\"msg_type\":\"0003\",\"source_dev_id\":\"\",\"user_id\":\"\",\"original_data_source\":\"SMART\",\"msg_queue_timestamp\":\"1558563172000\",\"source_system_id\":\"TRUST\"},\"body\":{\"event_type\":\"DEPARTURE\",\"gbtt_timestamp\":\"\",\"original_loc_stanox\":\"\",\"planned_timestamp\":\"1558566630000\",\"timetable_variation\":\"2\",\"original_loc_timestamp\":\"\",\"current_train_id\":\"\",\"delay_monitoring_point\":\"true\",\"next_report_run_time\":\"5\",\"reporting_stanox\":\"16460\",\"actual_timestamp\":\"1558566720000\",\"correction_ind\":\"false\",\"event_source\":\"AUTOMATIC\",\"train_file_address\":null,\"platform\":\"\",\"division_code\":\"20\",\"train_terminated\":\"false\",\"train_id\":\"369E21M722\",\"offroute_ind\":\"false\",\"variation_status\":\"LATE\",\"train_service_code\":\"21731000\",\"toc_id\":\"20\",\"loc_stanox\":\"16460\",\"auto_expected\":\"true\",\"direction_ind\":\"DOWN\",\"route\":\"1\",\"planned_event_type\":\"DEPARTURE\",\"next_report_stanox\":\"16416\",\"line_ind\":\"L\"}}]"}
#
# To make it easier to process we use jq here to extract just that field,
# convert from the escaped JSON into JSON, and explode the array into 
# separate messages
#
# The simplest way is to use kafkacat piping into itself via jq

kafkacat -b localhost:9092 -G tm_explode networkrail_TRAIN_MVT | \
 jq -c '.text|fromjson[]' | \
 kafkacat -b localhost:9092 -t networkrail_TRAIN_MVT_X -T -P
