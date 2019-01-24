#!/bin/sh


# Create test user
sqlplus sys/Admin123@//localhost:1521/ORCLPDB1 as sysdba <<- EOF
	CREATE USER connect_user IDENTIFIED BY asgard;
	GRANT CONNECT TO connect_user;
	GRANT CREATE SESSION TO connect_user;
	GRANT CREATE TABLE TO connect_user;
	GRANT CREATE SEQUENCE TO connect_user;
	GRANT CREATE TRIGGER TO connect_user;
	ALTER USER connect_user QUOTA 100M ON users;

	exit;
EOF

