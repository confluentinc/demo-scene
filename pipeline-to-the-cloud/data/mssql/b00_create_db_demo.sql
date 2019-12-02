USE [master]
GO
CREATE DATABASE demo;
GO
USE [demo]
GO
EXEC sys.sp_cdc_enable_db
GO

-- You can't use CDC on a contained DB. 
-- -------------------------------------
-- ALTER DATABASE [demo] SET CONTAINMENT = PARTIAL
-- GO
-- CREATE USER connect_user WITH PASSWORD = 'Asgard123';
-- GO
-- Msg 33233, Level 16, State 1, Server fc715095025d, Line 1
-- You can only create a user with a password in a contained database.

