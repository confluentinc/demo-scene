CREATE OR REPLACE STREAM inventory_commands (
  user_id VARCHAR KEY,
  action VARCHAR,
  item VARCHAR
) WITH (
  KAFKA_TOPIC = 'inventory_commands',
  VALUE_FORMAT = 'AVRO',
  VALUE_AVRO_SCHEMA_FULL_NAME='io.confluent.developer.adventure.InventoryCommandValue',
  PARTITIONS = 20
);
