create table demo.accounts (
	id INT,
	first_name VARCHAR(50),
	last_name VARCHAR(50),
	username VARCHAR(50),
	company VARCHAR(50),
	created_date DATE,
	UPDATE_TS TIMESTAMP DEFAULT CURRENT_TIMESTAMP 
);

CREATE TRIGGER accounts_updated_at_modtime BEFORE UPDATE ON demo.accounts FOR EACH ROW EXECUTE PROCEDURE public.update_updated_at_column();

GRANT SELECT ON demo.accounts TO connect_user;
