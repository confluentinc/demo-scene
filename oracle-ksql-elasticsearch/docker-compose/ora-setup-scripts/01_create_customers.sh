#!/bin/sh

echo 'Creating Debezium.customers table'

sqlplus Debezium/dbz@//localhost:1521/ORCLPDB1  <<- EOF

        create table CUSTOMERS (
                id INT PRIMARY KEY,
                first_name VARCHAR(50),
                last_name VARCHAR(50),
                email VARCHAR(50),
                gender VARCHAR(50),
                club_status VARCHAR(8),
                comments VARCHAR(90),
                create_ts timestamp DEFAULT CURRENT_TIMESTAMP ,
                update_ts timestamp DEFAULT CURRENT_TIMESTAMP 
        --        update_ts timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        );

  ALTER TABLE debezium.customers ADD SUPPLEMENTAL LOG DATA (ALL) COLUMNS;

  -- From https://xanpires.wordpress.com/2013/06/26/how-to-check-the-supplemental-log-information-in-oracle/
  COLUMN LOG_GROUP_NAME HEADING 'Log Group' FORMAT A20
  COLUMN TABLE_NAME HEADING 'Table' FORMAT A20
  COLUMN ALWAYS HEADING 'Type of Log Group' FORMAT A30

  SELECT LOG_GROUP_NAME, TABLE_NAME, DECODE(ALWAYS, 'ALWAYS', 'Unconditional', NULL, 'Conditional') ALWAYS FROM DBA_LOG_GROUPS;

  exit;
EOF
