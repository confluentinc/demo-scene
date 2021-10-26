CREATE OR REPLACE STREAM status_commands (
  user_id VARCHAR KEY,
  command VARCHAR
) WITH (
  KAFKA_TOPIC = 'status_commands',
  VALUE_FORMAT = 'AVRO',
  VALUE_AVRO_SCHEMA_FULL_NAME='io.confluent.developer.adventure.StatusCommandValue',
  PARTITIONS = 20
);
