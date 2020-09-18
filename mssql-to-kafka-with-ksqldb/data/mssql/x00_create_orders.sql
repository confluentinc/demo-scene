use [demo];

CREATE TABLE demo.dbo.ORDERS ( order_id INT, customer_id INT, order_ts DATE, order_total_usd DECIMAL(5,2), item VARCHAR(50) ); 
GO

EXEC sys.sp_cdc_enable_table
@source_schema = N'dbo',
@source_name   = N'ORDERS',
@role_name     = NULL,
@supports_net_changes = 0
GO

-- At this point you should get a row returned from this query
SELECT s.name AS Schema_Name, tb.name AS Table_Name , tb.object_id, tb.type, tb.type_desc, tb.is_tracked_by_cdc FROM sys.tables tb INNER JOIN sys.schemas s on s.schema_id = tb.schema_id WHERE tb.is_tracked_by_cdc = 1
GO
-- h/t William Prigol Lopes https://stackoverflow.com/a/61698148/350613