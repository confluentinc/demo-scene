create database security;

GRANT SELECT, INSERT, UPDATE, DELETE ON security.* TO connect_user;
GRANT ALL PRIVILEGES ON security.* TO 'debezium'@'%';
