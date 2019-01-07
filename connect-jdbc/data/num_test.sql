USE DEMO;

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

INSERT INTO NUM_TEST VALUES (42,42,100.01, -100.02, 100.03, 100.04);

SELECT * FROM NUM_TEST;

DESCRIBE NUM_TEST