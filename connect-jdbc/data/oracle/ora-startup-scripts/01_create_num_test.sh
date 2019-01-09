#!/bin/sh

sqlplus connect_user/asgard@//localhost:1521/ORCLPDB1  <<- EOF

  drop table num_test purge;
  
CREATE TABLE NUM_TEST (
	TXN_ID INT,
	CUSTOMER_ID INT,
	AMOUNT_01 DECIMAL(5,2),
	AMOUNT_02 NUMERIC(5,2), 
	AMOUNT_03 DECIMAL(5),
	AMOUNT_04 DECIMAL,
	AMOUNT_05 NUMBER(5,2), 
	AMOUNT_06 NUMBER(5), 
	AMOUNT_07 NUMBER
);
  
INSERT INTO NUM_TEST VALUES (42,42,100.01, -100.02, 100.03, 100.04, 100.05, 100.06, 100.07);

SELECT * FROM NUM_TEST;

DESCRIBE NUM_TEST;

EOF

#  Name                                      Null?    Type
#  ----------------------------------------- -------- ----------------------------
#  TXN_ID                                             NUMBER(38)
#  CUSTOMER_ID                                        NUMBER(38)
#  AMOUNT_01                                          NUMBER(5,2)
#  AMOUNT_02                                          NUMBER(5,2)
#  AMOUNT_03                                          NUMBER(5)
#  AMOUNT_04                                          NUMBER(38)
#  AMOUNT_05                                          NUMBER(5,2)
#  AMOUNT_06                                          NUMBER(5)
#  AMOUNT_07                                          NUMBER