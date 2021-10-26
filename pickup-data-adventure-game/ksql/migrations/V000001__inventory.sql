CREATE STREAM inventory (
  user_id VARCHAR KEY,
  item VARCHAR,
  held BOOLEAN
) WITH (
  KAFKA_TOPIC = 'inventory',
  VALUE_FORMAT = 'AVRO',
  VALUE_AVRO_SCHEMA_FULL_NAME='io.confluent.developer.adventure.InventoryValue',
  PARTITIONS = 20
);
