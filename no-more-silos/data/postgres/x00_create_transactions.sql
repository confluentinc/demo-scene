create table demo.transactions (
	txn_id INT,
	customer_id INT,
	amount DECIMAL(5,2),
	currency VARCHAR(50),
	txn_timestamp VARCHAR(50)
);

GRANT SELECT ON demo.transactions TO connect_user;
