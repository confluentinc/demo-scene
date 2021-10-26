CREATE OR REPLACE STREAM item_rules (
  source_item VARCHAR KEY,
  x INT,
  y INT,
  generates_item VARCHAR,
  description VARCHAR
) WITH (
  KAFKA_TOPIC = 'item_rules',
  VALUE_FORMAT = 'AVRO',
  VALUE_AVRO_SCHEMA_FULL_NAME='io.confluent.developer.adventure.ItemRulesValue',
  PARTITIONS = 1
);
