CREATE OR REPLACE TABLE dashboard
AS
  SELECT
    'sales' as metric,
    sum(price) AS total
  FROM purchases
  GROUP BY 'sales'
WITH (
  KAFKA_TOPIC='dashboard',
  PARTITIONS=3,
  REPLICAS=3,
  VALUE_AVRO_SCHEMA_FULL_NAME='io.confluent.developer.dashboard.DashboardValue',
  VALUE_FORMAT='avro'
);
