#!/usr/bin/env bash

curl -i -X POST -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:28083/connectors/ \
    -d '{
  "name": "sink-s3-train-movements-v00",
  "config": {
		"connector.class": "io.confluent.connect.s3.S3SinkConnector",
		"key.converter":"org.apache.kafka.connect.storage.StringConverter",
		"tasks.max": "1",
		"topics": "TRAIN_MOVEMENTS_ACTIVATIONS_SCHEDULE_00",
		"s3.region": "us-west-2",
		"s3.bucket.name": "rmoff-rail-streaming-demo",
		"flush.size": "3",
		"storage.class": "io.confluent.connect.s3.storage.S3Storage",
		"format.class": "io.confluent.connect.s3.format.avro.AvroFormat",
		"schema.generator.class": "io.confluent.connect.storage.hive.schema.DefaultSchemaGenerator",
		"schema.compatibility": "NONE",
		"partitioner.class": "io.confluent.connect.storage.partitioner.TimeBasedPartitioner",
 		"path.format":"\'year\'=YYYY/\'month\'=MM/\'day\'=dd",
		"timestamp.extractor":"Record",
		"partition.duration.ms": 300000,
		"locale":"en",
		"timezone": "UTC"
	}
}'

