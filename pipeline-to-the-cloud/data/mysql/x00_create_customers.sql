USE demo;
create table customers (
	id INT,
	first_name VARCHAR(50),
	last_name VARCHAR(50),
	birthdate DATE,
	email VARCHAR(50),
	company VARCHAR(50),
	street_address VARCHAR(50),
	city VARCHAR(50),
	country VARCHAR(50),
	fav_movie VARCHAR(255),
	fav_colour VARCHAR(50),
	fav_animal VARCHAR(50)
	PRIMARY KEY (id)
);
