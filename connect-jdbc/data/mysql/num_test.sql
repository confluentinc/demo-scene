USE demo;

CREATE TABLE NUM_TEST (
	TXN_ID INT,
	CUSTOMER_ID INT,
	AMOUNT_01 DECIMAL(5,2),
	AMOUNT_02 NUMERIC(5,2), -- https://dev.mysql.com/doc/refman/8.0/en/fixed-point-types.html numeric == decimal
	AMOUNT_03 DECIMAL(5),
	AMOUNT_04 DECIMAL
);

-- The precision represents the number of significant digits that are stored for values
-- the scale represents the number of digits that can be stored following the decimal point.

INSERT INTO NUM_TEST VALUES (42,42,100.01, -100.02, 100, 100);

SELECT * FROM NUM_TEST;

DESCRIBE NUM_TEST;

-- mysql> DESCRIBE NUM_TEST;
-- +-------------+---------------+------+-----+---------+-------+
-- | Field       | Type          | Null | Key | Default | Extra |
-- +-------------+---------------+------+-----+---------+-------+
-- | TXN_ID      | int(11)       | YES  |     | NULL    |       |
-- | CUSTOMER_ID | int(11)       | YES  |     | NULL    |       |
-- | AMOUNT_01   | decimal(5,2)  | YES  |     | NULL    |       |
-- | AMOUNT_02   | decimal(5,2)  | YES  |     | NULL    |       |
-- | AMOUNT_03   | decimal(5,0)  | YES  |     | NULL    |       |
-- | AMOUNT_04   | decimal(10,0) | YES  |     | NULL    |       |
-- +-------------+---------------+------+-----+---------+-------+
-- 6 rows in set (0.01 sec)
