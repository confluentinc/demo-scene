CREATE OR REPLACE TABLE dashboard AS
  SELECT
    sum(price) AS total,
    'sales'
  FROM purchases
  GROUP BY 'sales';
