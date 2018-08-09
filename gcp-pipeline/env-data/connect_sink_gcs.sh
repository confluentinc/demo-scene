curl -X "POST" "http://localhost:8083/connectors/" \
     -H "Content-Type: application/json" \
    -d '{
  "name": "sink_gcs_environment-data",
  "config": {
    "connector.class": "io.confluent.connect.gcs.GcsSinkConnector",
    "key.converter":"org.apache.kafka.connect.storage.StringConverter",
    "value.converter":"io.confluent.connect.avro.AvroConverter",
    "value.converter.schema.registry.url":"http://schema-registry:8081",
    "tasks.max": "1",
    "topics": "ENVIRONMENT_DATA",
    "gcs.bucket.name": "rmoff-environment-data",
    "gcs.part.size": "5242880",
    "flush.size": "16",
    "gcs.credentials.path": "/root/creds/gcp_creds.json",
    "storage.class": "io.confluent.connect.gcs.storage.GcsStorage",
    "format.class": "io.confluent.connect.gcs.format.json.JsonFormat",
    "partitioner.class": "io.confluent.connect.storage.partitioner.DefaultPartitioner",
    "confluent.topic.bootstrap.servers":"localhost:9092",
    "confluent.topic.replication.factor":1
    }
}'