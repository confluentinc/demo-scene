#!/bin/bash

# docker exec twitterstreams_connect_1 kafka-console-consumer --bootstrap-server kafka1:9092 --from-beginning --topic twitter_json_01 --consumer-property interceptor.classes=io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor  |jq '.Text'

docker exec twitter-streams_connect_1 kafka-console-consumer --bootstrap-server kafka1:9091 --consumer.config=/usr/share/consumer.properties --from-beginning --topic twitter_json_01 | jq '.Text'
