#!/bin/bash

curl -X POST http://localhost:8083/connectors \
  -H 'Content-Type: application/json' \
  -d @- << EOF
{
  "name": "account-jdbc-source",  
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": false,
    "tasks.max": "1",
    "connection.url": "jdbc:postgresql://account-db:5432/account-store",
    "connection.user": "account-store",
    "connection.password": "account-store",
    "mode": "timestamp",
    "timestamp.column.name": "update_date",
    "table.whitelist": "account",
    "topic.prefix": "",
    "name": "account-jdbc-source",
    "transforms": "createKey,extractString,renameFields",
    "transforms.createKey.type":"org.apache.kafka.connect.transforms.ValueToKey",
    "transforms.createKey.fields":"number",
    "transforms.extractString.type":"org.apache.kafka.connect.transforms.ExtractField\$Key",
    "transforms.extractString.field":"number",
    "transforms.renameFields.type": "org.apache.kafka.connect.transforms.ReplaceField\$Value",
    "transforms.renameFields.renames": "city_address:cityAddress,country_address:countryAddress,creation_date:creationDate,first_name:firstName,last_name:lastName,number_address:numberAddress,street_address:streetAddress,update_date:updateDate"
  }
}
EOF