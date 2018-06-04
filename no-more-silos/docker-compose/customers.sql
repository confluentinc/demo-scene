GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'replicator' IDENTIFIED BY 'replpass';
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT  ON *.* TO 'debezium' IDENTIFIED BY 'dbz';

# Create the database that we'll use to populate data and watch the effect in the binlog
CREATE DATABASE demo;
GRANT ALL PRIVILEGES ON demo.* TO 'mysqluser'@'%';

use demo;

create table customers (
	id INT PRIMARY KEY,
	first_name VARCHAR(50),
	last_name VARCHAR(50),
	email VARCHAR(50),
	gender VARCHAR(50),
	comments VARCHAR(90),
	create_ts timestamp DEFAULT CURRENT_TIMESTAMP ,
	update_ts timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
insert into customers (id, first_name, last_name, email, gender, comments) values (1, 'Bibby', 'Argabrite', 'bargabrite0@google.com.hk', 'Female', 'Reactive exuding productivity');
insert into customers (id, first_name, last_name, email, gender, comments) values (2, 'Auberon', 'Sulland', 'asulland1@slideshare.net', 'Male', 'Organized context-sensitive Graphical User Interface');
insert into customers (id, first_name, last_name, email, gender, comments) values (3, 'Marv', 'Dalrymple', 'mdalrymple2@macromedia.com', 'Male', 'Versatile didactic pricing structure');
insert into customers (id, first_name, last_name, email, gender, comments) values (4, 'Nolana', 'Yeeles', 'nyeeles3@drupal.org', 'Female', 'Adaptive real-time archive');
insert into customers (id, first_name, last_name, email, gender, comments) values (5, 'Modestia', 'Coltart', 'mcoltart4@scribd.com', 'Female', 'Reverse-engineered non-volatile success');
insert into customers (id, first_name, last_name, email, gender, comments) values (6, 'Bram', 'Acaster', 'bacaster5@pagesperso-orange.fr', 'Male', 'Robust systematic support');
insert into customers (id, first_name, last_name, email, gender, comments) values (7, 'Marigold', 'Veld', 'mveld6@pinterest.com', 'Female', 'Sharable logistical installation');
insert into customers (id, first_name, last_name, email, gender, comments) values (8, 'Ruperto', 'Matteotti', 'rmatteotti7@diigo.com', 'Male', 'Diverse client-server conglomeration');
