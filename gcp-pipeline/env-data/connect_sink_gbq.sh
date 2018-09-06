curl -X "POST" "http://localhost:8083/connectors/" \
     -H "Content-Type: application/json" \
    -d '{
  "name": "sink_gbq_environment-data",
  "config": {
    "connector.class":"com.wepay.kafka.connect.bigquery.BigQuerySinkConnector",
    "topics": "ENVIRONMENT_DATA_KEYED",
    "key.converter":"org.apache.kafka.connect.storage.StringConverter",
    "value.converter":"io.confluent.connect.avro.AvroConverter",
    "value.converter.schema.registry.url":"http://schema-registry:8081",
    "schemaRetriever":"com.wepay.kafka.connect.bigquery.schemaregistry.schemaretriever.SchemaRegistrySchemaRetriever",
    "schemaRegistryLocation":"http://schema-registry:8081",
    "tasks.max":"1",
    "sanitizeTopics":"true",
    "autoCreateTables":"true",
    "autoUpdateSchemas":"true",
    "bufferSize":"100000",
    "maxWriteSize":"10000",
    "tableWriteWait":"1000",
    "project":"GCP_PROJECT_NAME",
    "datasets":".*=environment_data",
    "keyfile":"/root/creds/gcp_creds.json"
    }
}'
