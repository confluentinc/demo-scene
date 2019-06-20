#!/usr/bin/env bash

kafkacat -b localhost:9092 -t alert_config -P -K: <<EOF
BEN RHYDDING:{"STATION":"BEN RHYDDING","ALERT_OVER_MINS":"5"}
LEEDS:{"STATION":"LEEDS","ALERT_OVER_MINS":"30"}
EOF