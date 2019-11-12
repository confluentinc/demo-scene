#!/bin/bash

while [ 1 -eq 1 ]
do awk '{print $0;system("sleep 0.75");}' ./data/dummy_data.kcat | \
    kafkacat -b localhost:9092 -P -t data_mqtt -K:
done;