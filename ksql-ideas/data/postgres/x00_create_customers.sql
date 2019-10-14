create table demo.customers (
	id INT PRIMARY KEY,
	first_name VARCHAR(50),
	last_name VARCHAR(50),
	email VARCHAR(50),
	gender VARCHAR(50),
	comments VARCHAR(90),
	UPDATE_TS TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER customers_updated_at_modtime BEFORE UPDATE ON demo.customers FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

GRANT SELECT ON demo.customers TO connect_user;
