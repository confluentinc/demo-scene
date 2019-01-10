use demo;
CREATE TABLE demo.dbo.NUM_TEST ( TXN_ID INT, CUSTOMER_ID INT, AMOUNT_01 DECIMAL(5,2), AMOUNT_02 NUMERIC(5,2), AMOUNT_03 DECIMAL(5), AMOUNT_04 DECIMAL );
GO
GRANT SELECT ON demo.dbo.NUM_TEST TO [connect_user]
GO
INSERT INTO demo.dbo.NUM_TEST VALUES (42,42,100.01, -100.02, 100, 100);
GO
SELECT * FROM demo.dbo.NUM_TEST;
GO
sp_help NUM_TEST;
GO
-- 1> sp_help NUM_TEST;
-- 2> go
-- Name                                                                                                                             Owner                                                                                                                            Type                            Created_datetime
-- -------------------------------------------------------------------------------------------------------------------------------- -------------------------------------------------------------------------------------------------------------------------------- ------------------------------- -----------------------
-- NUM_TEST                                                                                                                         dbo                                                                                                                              user table                      2019-01-08 10:58:56.867


-- Column_name                                                                                                                      Type                                                                                                                             Computed                            Length      Prec  Scale Nullable                            TrimTrailingBlanks                  FixedLenNullInSource                Collation
-- -------------------------------------------------------------------------------------------------------------------------------- -------------------------------------------------------------------------------------------------------------------------------- ----------------------------------- ----------- ----- ----- ----------------------------------- ----------------------------------- ----------------------------------- --------------------------------------------------------------------------------------------------------------------------------
-- TXN_ID                                                                                                                           int                                                                                                                              no                                            4 10    0     yes                                 (n/a)                               (n/a)                               NULL
-- CUSTOMER_ID                                                                                                                      int                                                                                                                              no                                            4 10    0     yes                                 (n/a)                               (n/a)                               NULL
-- AMOUNT_01                                                                                                                        decimal                                                                                                                          no                                            5 5     2     yes                                 (n/a)                               (n/a)                               NULL
-- AMOUNT_02                                                                                                                        numeric                                                                                                                          no                                            5 5     2     yes                                 (n/a)                               (n/a)                               NULL
-- AMOUNT_03                                                                                                                        decimal                                                                                                                          no                                            5 5     0     yes                                 (n/a)                               (n/a)                               NULL
-- AMOUNT_04                                                                                                                        decimal                                                                                                                          no                                            9 18    0     yes                                 (n/a)                               (n/a)                               NULL
