USE [master]
GO
CREATE DATABASE demo;
GO
USE [demo]
EXEC sys.sp_cdc_enable_db
GO

-- Run this to confirm that CDC is now enabled: 
SELECT name, is_cdc_enabled FROM sys.databases; 
GO