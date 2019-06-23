#!/usr/bin/env bash

curl -i -X PUT -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:8083/connectors/replicator-source/config \
    -d '
        {
	"connector.class": "io.confluent.connect.replicator.ReplicatorSourceConnector",
	"key.converter": "io.confluent.connect.replicator.util.ByteArrayConverter",
	"value.converter": "io.confluent.connect.replicator.util.ByteArrayConverter",
	"header.converter": "io.confluent.connect.replicator.util.ByteArrayConverter",
	"src.kafka.bootstrap.servers": "proxmox01.moffatt.me:9092",
	"dest.kafka.bootstrap.servers": "kafka:29092",
	"topic.whitelist": "TRAIN_MOVEMENTS_ACTIVATIONS_SCHEDULE_00",
	"topic.rename.format":"${topic}",
	"confluent.license":"",
	"confluent.topic.bootstrap.servers":"kafka:29092",
	"confluent.topic.replication.factor":1
	}'
