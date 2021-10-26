CREATE OR REPLACE STREAM location_data (
  x INT KEY,
  y INT KEY,
  description VARCHAR,
  objects ARRAY<VARCHAR>
) WITH (
  KAFKA_TOPIC = 'location_data',
  KEY_FORMAT = 'JSON',
  VALUE_FORMAT = 'AVRO',
  VALUE_AVRO_SCHEMA_FULL_NAME='io.confluent.developer.adventure.LocationDataValue',
  PARTITIONS = 1
);
