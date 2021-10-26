CREATE OR REPLACE STREAM commands (
  user_id VARCHAR KEY,
  command VARCHAR
) WITH (
  KAFKA_TOPIC = 'commands',
  VALUE_FORMAT = 'AVRO',
  VALUE_AVRO_SCHEMA_FULL_NAME='io.confluent.developer.adventure.CommandValue',
  PARTITIONS = 20
);
