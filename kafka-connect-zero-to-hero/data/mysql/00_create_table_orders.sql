GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'replicator' IDENTIFIED BY 'replpass';
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT  ON *.* TO 'debezium' IDENTIFIED BY 'dbz';

# Create the database that we'll use to populate data and watch the effect in the binlog
CREATE DATABASE demo;
GRANT ALL PRIVILEGES ON demo.* TO 'debezium'@'%';

use demo;

create table ORDERS (
	id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	order_id INT,
	customer_id INT,
	order_total_usd DECIMAL(11,2),
	make VARCHAR(50),
	model VARCHAR(50),
	delivery_city VARCHAR(50),
	delivery_company VARCHAR(50),
	delivery_address VARCHAR(50),
	CREATE_TS TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	UPDATE_TS TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
