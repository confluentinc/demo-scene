#!/bin/sh

curl -i -X POST -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:18083/connectors/ \
    -d '{
        "name": "jdbc_source_mysql_foobar_01",
        "config": {
                "_comment": "The JDBC connector class. Do not change this if you want to use the JDBC Source.",
                "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",

                "_comment": " --- JDBC-specific configuration below here  --- ",
                "_comment": "JDBC connection URL. This will vary by RDBMS. Consult your DB docs for more information",
                "connection.url": "jdbc:mysql://mysql:3306/demo?user=debezium&password=dbz",

                "_comment": "Which table(s) to include",
                "table.whitelist": "CUSTOMERS",

                "_comment": "Pull all rows based on an timestamp column. You can also do bulk or incrementing column-based extracts. For more information, see http://docs.confluent.io/current/connect/connect-jdbc/docs/source_config_options.html#mode",
                "mode": "timestamp",

                "_comment": "Which column has the timestamp value to use?  ",
                "timestamp.column.name": "update_ts",

                "_comment": "If the column is not defined as NOT NULL, tell the connector to ignore this  ",
                "validate.non.null": "false",

                "_comment": "The Kafka topic will be made up of this prefix, plus the table name  ",
                "topic.prefix": "mysql-jdbc-"
        }
}
'
