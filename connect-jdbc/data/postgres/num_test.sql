CREATE TABLE demo.NUM_TEST (
	TXN_ID INT,
	CUSTOMER_ID INT,
	AMOUNT_01 DECIMAL(5,2),
	AMOUNT_02 NUMERIC(5,2), 
	AMOUNT_03 DECIMAL(5),
	AMOUNT_04 DECIMAL
);
GRANT SELECT ON demo.NUM_TEST TO connect_user;

-- The precision represents the number of significant digits that are stored for values
-- the scale represents the number of digits that can be stored following the decimal point.

INSERT INTO demo.NUM_TEST VALUES (42,42,100.01, -100.02, 100, 100);

SELECT * FROM demo.NUM_TEST;

\d demo.NUM_TEST

-- demo=# \d demo.NUM_TEST
--                    Table "public.demo.NUM_TEST"
--    Column    |     Type     | Collation | Nullable | Default
-- -------------+--------------+-----------+----------+---------
--  txn_id      | integer      |           |          |
--  customer_id | integer      |           |          |
--  amount_01   | numeric(5,2) |           |          |
--  amount_02   | numeric(5,2) |           |          |
--  amount_03   | numeric(5,0) |           |          |
--  amount_04   | numeric      |           |          |