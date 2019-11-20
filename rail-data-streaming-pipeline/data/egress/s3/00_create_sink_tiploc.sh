#!/usr/bin/env bash

curl -i -X PUT -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:28083/connectors/sink-s3-tiploc-v00/config \
    -d '
 {
		"connector.class": "io.confluent.connect.s3.S3SinkConnector",
		"key.converter":"org.apache.kafka.connect.storage.StringConverter",
		"tasks.max": "1",
		"topics": "TIPLOC_FLAT_KEYED",
		"s3.region": "us-west-2",
		"s3.bucket.name": "rmoff-rail-streaming-demo",
		"flush.size": "65536",
		"storage.class": "io.confluent.connect.s3.storage.S3Storage",
		"format.class": "io.confluent.connect.s3.format.avro.AvroFormat",
		"schema.generator.class": "io.confluent.connect.storage.hive.schema.DefaultSchemaGenerator",
		"schema.compatibility": "NONE",
	        "partitioner.class": "io.confluent.connect.storage.partitioner.DefaultPartitioner"
	}
'

