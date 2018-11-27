#!/bin/sh


sqlplus sys/Admin123@//localhost:1521/ORCLPDB1 as sysdba <<- EOF
  ALTER TABLE debezium.customers ADD SUPPLEMENTAL LOG DATA (ALL) COLUMNS;
	exit;
EOF