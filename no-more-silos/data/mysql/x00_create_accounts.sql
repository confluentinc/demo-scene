use demo;

create table accounts (
	id INT,
	first_name VARCHAR(50),
	last_name VARCHAR(50),
	username VARCHAR(50),
	company VARCHAR(50),
	created_date DATE,
	UPDATE_TS TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
