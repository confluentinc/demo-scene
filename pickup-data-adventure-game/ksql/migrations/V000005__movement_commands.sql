CREATE OR REPLACE STREAM movement_commands (
  user_id VARCHAR KEY,
  direction VARCHAR
) WITH (
  KAFKA_TOPIC = 'movement_commands',
  VALUE_FORMAT = 'AVRO',
  VALUE_AVRO_SCHEMA_FULL_NAME='io.confluent.developer.adventure.MovementCommandValue',
  PARTITIONS = 20
);
