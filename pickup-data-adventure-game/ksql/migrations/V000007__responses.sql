CREATE OR REPLACE STREAM responses (
  user_id VARCHAR KEY,
  source VARCHAR,
  response VARCHAR
) WITH (
  KAFKA_TOPIC = 'responses',
  VALUE_FORMAT = 'AVRO',
  VALUE_AVRO_SCHEMA_FULL_NAME='io.confluent.developer.adventure.ResponseValue',
  PARTITIONS = 20
);
