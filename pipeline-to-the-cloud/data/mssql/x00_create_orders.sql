use demo;

CREATE TABLE demo.dbo.ORDERS (
	order_id INT,
	customer_id INT,
	order_ts DATE,
	order_total_usd DECIMAL(5,2),
	item VARCHAR(50)
);
GO

EXEC sys.sp_cdc_enable_table
@source_schema = N'dbo',
@source_name   = N'ORDERS',
@role_name     = NULL,
@supports_net_changes = 0
GO