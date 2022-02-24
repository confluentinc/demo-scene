CREATE OR REPLACE TABLE dashboard
WITH (VALUE_AVRO_SCHEMA_FULL_NAME='io.confluent.developer.dashboard.DashboardValue')
AS
  SELECT
    'sales' as metric,
    sum(price) AS total
  FROM purchases
  GROUP BY 'sales';
