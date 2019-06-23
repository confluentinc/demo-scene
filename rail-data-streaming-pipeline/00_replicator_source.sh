#!/usr/bin/env bash


curl -i -X PUT -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:48083/connectors/replicator-source/config \
    -d '
	{
	  "connector.class": "io.confluent.connect.replicator.ReplicatorSourceConnector",
	  "key.converter": "io.confluent.connect.replicator.util.ByteArrayConverter",
	  "value.converter": "io.confluent.connect.replicator.util.ByteArrayConverter",
	  "header.converter": "io.confluent.connect.replicator.util.ByteArrayConverter",
	  "src.kafka.bootstrap.servers": "kafka:29092",
	  "dest.kafka.bootstrap.servers": "172.27.230.24:39092",
	  "topic.whitelist": "networkrail_TRAIN_MVT_X",
              "confluent.license": "",
              "confluent.topic.bootstrap.servers": "kafka:29092",
              "confluent.topic.replication.factor": 1
	}'
