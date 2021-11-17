CREATE OR REPLACE STREAM purchases (
  sku VARCHAR KEY,
  name VARCHAR,
  description VARCHAR,
  price DECIMAL(10,2)
)
WITH (
  KAFKA_TOPIC = 'purchases',
  VALUE_FORMAT = 'AVRO',
  VALUE_AVRO_SCHEMA_FULL_NAME='io.confluent.developer.dashboard.Purchase',
  PARTITIONS = 3
);
