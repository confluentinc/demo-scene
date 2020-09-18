Check that the MSSQL server plugin has been installed: 

      ➜ curl -s localhost:8083/connector-plugins|jq '.[].class'

      "io.debezium.connector.sqlserver.SqlServerConnector"

Launch ksqlDB CLI

      docker exec --interactive --tty ksqldb ksql http://localhost:8088

Create the connector


      CREATE SOURCE CONNECTOR SOURCE_MSSQL_ORDERS_01 WITH (
            'connector.class' = 'io.debezium.connector.sqlserver.SqlServerConnector', 
            'database.hostname' = 'mssql',
            'database.port' = '1433',
            'database.user' = 'sa',
            'database.password' = 'Admin123',
            'database.dbname' = 'demo',
            'database.server.name' = 'mssql',
            'table.whitelist' = 'dbo.orders',
            'database.history.kafka.bootstrap.servers' = 'broker:29092',
            'database.history.kafka.topic' = 'dbz_dbhistory.mssql.asgard-01',
            'decimal.handling.mode' = 'double'
      );
