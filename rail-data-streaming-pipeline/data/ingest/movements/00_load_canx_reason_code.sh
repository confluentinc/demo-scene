#!/usr/bin/env bash

kafkacat -b localhost:9092 -t canx_reason_code -P -K: -l canx_reason_code.dat