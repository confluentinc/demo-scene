#!/bin/bash 

kafkacat -b localhost:9092 -t data_mqtt-import -K: -P -T -l ./data/dummy_data.kcat
