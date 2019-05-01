create database demo;

GRANT  SELECT, INSERT, UPDATE, DELETE ON demo.* TO connect_user;
GRANT ALL PRIVILEGES ON demo.* TO 'debezium'@'%';
