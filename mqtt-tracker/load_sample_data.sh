#!/bin/bash 

kafkacat -b localhost:9092 -t data_mqtt-import -K: -P -T -l ./data/export_20190929.kcat 
