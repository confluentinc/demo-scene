#!/bin/bash
wait-for-url() {
    echo "Testing $1"
    timeout -s TERM 500 bash -c \
    'while [[ "$(curl -s -o /dev/null -L -w ''%{http_code}'' ${0})" != "200" ]];\
    do echo "Waiting for ${0}" && sleep 2;\
    done' ${1}
    echo "OK!"
    curl -I $1
}

function wait-for-schemas(){
  #array=( one two three )
  for i in "${TOPICS_TO_CHECK[@]}"
  do
    echo "\n\n⏳Waiting for schema $i-value  (topic: $i )\n"
    wait-for-url http://localhost:8081/subjects/$i-value/versions
  done
}


function wait-for-topics(){

  echo -e "\n\n⏳ Waiting for Topics to be created\n"
  for i in "${TOPICS_TO_CHECK[@]}"
  do
    until [ $(docker-compose exec -T ksqldb-cli ksql http://ksqldb-server-ccloud:8088 <<< "show topics;" | grep -c "$i") -ge 1 ]
    do 
      echo -e $(date) "Topic do not exist: " $(docker-compose exec -T ksqldb-cli ksql http://ksqldb-server-ccloud:8088 <<< "show topics;" | grep -c "$i") " (waiting for topics $i)"
      sleep 5
    done
  done


}

function wait-for-streams(){

  echo -e "\n\n⏳ Waiting for STREAM to be created\n"
  for i in "${STREAMS_TO_CHECK[@]}"
  do
    until [ $(docker-compose exec -T ksqldb-cli ksql http://ksqldb-server-ccloud:8088 <<< "show streams;" | grep -c "$i") -ge 1 ]
    do 
      echo -e $(date) "STREAM do not exist: " $(docker-compose exec -T ksqldb-cli ksql http://ksqldb-server-ccloud:8088 <<< "show STREAMS;" | grep -c "$i") " (waiting for STREAM $i)"
      sleep 5
    done
  done


}

#wait-for-url http://localhost:18083/connectors/
verify-connect-cluster() {
# Verify Kafka Connect Worker has started within 120 seconds
  MAX_WAIT=500
  CUR_WAIT=0
  CONNECT_DOCKER_NAME=$1
  echo "Waiting up to $MAX_WAIT seconds for Kafka Connect Worker to start"
  while [[ ! $(docker-compose logs $CONNECT_DOCKER_NAME) =~ "Kafka Connect started" ]]; do
    sleep 3
    CUR_WAIT=$(( CUR_WAIT+3 ))
    if [[ "$CUR_WAIT" -gt "$MAX_WAIT" ]]; then
      echo -e "\nERROR: The logs in Kafka Connect container do not show 'Kafka Connect started'. Please troubleshoot with 'docker-compose ps' and 'docker-compose logs'.\n"
      exit 1
    fi
  done
}

cd /home/dc01/.workshop/docker

# Checking DOCKER Containers status
docker ps --format "table {{.ID}}\t{{.Names}}\t{{.RunningFor}}\t{{.Status}}"

# Start producing sample transactions on mysql
docker exec -dit db-trans-simulator sh -c "python -u /simulate_dbtrans.py > /proc/1/fd/1 2>&1"

# Create the topics manually! TODO with retention one hour

verify-connect-cluster kafka-connect-onprem

# Configure CDC connector
curl -i -X POST -H "Accept:application/json" \
  -H  "Content-Type:application/json" http://localhost:18083/connectors/ \
  -d '{
    "name": "mysql-source-connector",
    "config": {
          "connector.class": "io.debezium.connector.mysql.MySqlConnector",
          "database.hostname": "mysql",
          "database.port": "3306",
          "database.user": "mysqluser",
          "database.password": "mysqlpw",
          "database.server.id": "12345",
          "database.server.name": "dc01",
          "database.whitelist": "orders",
          "table.blacklist": "orders.dc01_out_of_stock_events",
          "database.history.kafka.bootstrap.servers": "broker:29092",
          "database.history.kafka.topic": "debezium_dbhistory" ,
          "include.schema.changes": "false",
          "snapshot.mode": "when_needed",
          "transforms": "unwrap,sourcedc,TopicRename,extractKey",
          "transforms.unwrap.type": "io.debezium.transforms.UnwrapFromEnvelope",
          "transforms.sourcedc.type":"org.apache.kafka.connect.transforms.InsertField$Value",
          "transforms.sourcedc.static.field":"sourcedc",
          "transforms.sourcedc.static.value":"dc01",
          "transforms.TopicRename.type": "org.apache.kafka.connect.transforms.RegexRouter",
          "transforms.TopicRename.regex": "(.*)\\.(.*)\\.(.*)",
          "transforms.TopicRename.replacement": "$1_$3",
          "transforms.extractKey.type": "org.apache.kafka.connect.transforms.ExtractField$Key",
          "transforms.extractKey.field": "id",
          "key.converter": "org.apache.kafka.connect.converters.IntegerConverter"
      }
  }'


verify-connect-cluster kafka-connect-ccloud

# Configure Confluent Replicator
curl -i -X POST -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:18084/connectors/ \
    -d '{
        "name": "replicator-dc01-to-ccloud",
        "config": {
          "connector.class": "io.confluent.connect.replicator.ReplicatorSourceConnector",
          "key.converter": "io.confluent.connect.replicator.util.ByteArrayConverter",
          "value.converter": "io.confluent.connect.replicator.util.ByteArrayConverter",
          "topic.config.sync": false,
          "topic.regex": "dc[0-9][0-9][_].*",
          "topic.blacklist": "dc01_out_of_stock_events",
          "dest.kafka.bootstrap.servers": "${file:/secrets.properties:CCLOUD_CLUSTER_ENDPOINT}",
          "dest.kafka.security.protocol": "SASL_SSL",
          "dest.kafka.sasl.mechanism": "PLAIN",
          "dest.kafka.sasl.jaas.config": "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"${file:/secrets.properties:CCLOUD_API_KEY}\" password=\"${file:/secrets.properties:CCLOUD_API_SECRET}\";",
          "dest.kafka.replication.factor": 3,
          "src.kafka.bootstrap.servers": "broker:29092",
          "src.consumer.group.id": "replicator-dc01-to-ccloud",
          "src.consumer.interceptor.classes": "io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor",
          "src.consumer.confluent.monitoring.interceptor.bootstrap.servers": "broker:29092",
          "src.kafka.timestamps.producer.interceptor.classes": "io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor",
          "src.kafka.timestamps.producer.confluent.monitoring.interceptor.bootstrap.servers": "broker:29092",
          "tasks.max": "1"
        }
    }'


wait-for-url http://localhost:8088/info

TOPICS_TO_CHECK=(
  "dc01_customers"
  "dc01_products" 
  "dc01_purchase_order_details" 
  "dc01_purchase_orders" 
  "dc01_sales_order_details" 
  "dc01_sales_orders"
  "dc01_suppliers" 
)

wait-for-schemas

echo "---Start Running KSQL scripts..."
echo "------KSQL Create Initial Streams..."
docker-compose exec -T ksqldb-cli ksql http://ksqldb-server-ccloud:8088 << EOF
CREATE STREAM sales_orders WITH (KAFKA_TOPIC='dc01_sales_orders', VALUE_FORMAT='AVRO');
CREATE STREAM sales_order_details WITH (KAFKA_TOPIC='dc01_sales_order_details', VALUE_FORMAT='AVRO');
CREATE STREAM purchase_orders WITH (KAFKA_TOPIC='dc01_purchase_orders', VALUE_FORMAT='AVRO');
CREATE STREAM purchase_order_details WITH (KAFKA_TOPIC='dc01_purchase_order_details', VALUE_FORMAT='AVRO');
CREATE STREAM products WITH (KAFKA_TOPIC='dc01_products', VALUE_FORMAT='AVRO');
CREATE STREAM customers WITH (KAFKA_TOPIC='dc01_customers', VALUE_FORMAT='AVRO');
CREATE STREAM suppliers WITH (KAFKA_TOPIC='dc01_suppliers', VALUE_FORMAT='AVRO');
EOF

docker-compose exec -T ksqldb-cli ksql http://ksqldb-server-ccloud:8088 << EOF
CREATE TABLE customers_tbl (
  ROWKEY      INT PRIMARY KEY,
  FIRST_NAME  VARCHAR,
  LAST_NAME   VARCHAR,
  EMAIL       VARCHAR,
  CITY        VARCHAR,
  COUNTRY     VARCHAR,
  SOURCEDC    VARCHAR
)
WITH (
  KAFKA_TOPIC='dc01_customers',
  VALUE_FORMAT='AVRO'
);

CREATE TABLE suppliers_tbl (
  ROWKEY      INT PRIMARY KEY,
  NAME        VARCHAR,
  EMAIL       VARCHAR,
  CITY        VARCHAR,
  COUNTRY     VARCHAR,
  SOURCEDC    VARCHAR
)
WITH (
  KAFKA_TOPIC='dc01_suppliers',
  VALUE_FORMAT='AVRO'
);

CREATE TABLE products_tbl (
  ROWKEY      INT PRIMARY KEY,
  NAME        VARCHAR,
  DESCRIPTION VARCHAR,
  PRICE       DECIMAL(10,2),
  COST        DECIMAL(10,2),
  SOURCEDC    VARCHAR
)
WITH (
  KAFKA_TOPIC='dc01_products',
  VALUE_FORMAT='AVRO'
);
EOF

docker-compose exec -T ksqldb-cli ksql http://ksqldb-server-ccloud:8088 << EOF
SET 'auto.offset.reset'='earliest';
CREATE STREAM sales_enriched WITH (PARTITIONS = 1, KAFKA_TOPIC = 'dc01_sales_enriched') AS SELECT
    o.id order_id,
    od.id order_details_id,
    o.order_date,
    od.product_id product_id,
    pt.name product_name,
    pt.description product_desc,
    od.price product_price,
    od.quantity product_qty,
    o.customer_id customer_id,
    ct.first_name customer_fname,
    ct.last_name customer_lname,
    ct.email customer_email,
    ct.city customer_city,
    ct.country customer_country
FROM sales_orders o
INNER JOIN sales_order_details od WITHIN 1 SECONDS ON (o.id = od.sales_order_id)
INNER JOIN customers_tbl ct ON (o.customer_id = ct.rowkey)
INNER JOIN products_tbl pt ON (od.product_id = pt.rowkey);


SET 'auto.offset.reset'='earliest';
CREATE STREAM purchases_enriched WITH (PARTITIONS = 1, KAFKA_TOPIC = 'dc01_purchases_enriched') AS SELECT
    o.id order_id,
    od.id order_details_id,
    o.order_date,
    od.product_id product_id,
    pt.name product_name,
    pt.description product_desc,
    od.cost product_cost,
    od.quantity product_qty,
    o.supplier_id supplier_id,
    st.name supplier_name,
    st.email supplier_email,
    st.city supplier_city,
    st.country supplier_country
FROM purchase_orders o
INNER JOIN purchase_order_details od WITHIN 1 SECONDS ON (o.id = od.purchase_order_id)
INNER JOIN suppliers_tbl st ON (o.supplier_id = st.rowkey)
INNER JOIN products_tbl pt ON (od.product_id = pt.rowkey);
EOF

TOPICS_TO_CHECK=(
  "dc01_sales_enriched"
)

wait-for-schemas

docker-compose exec -T ksqldb-cli ksql http://ksqldb-server-ccloud:8088 << EOF
SET 'auto.offset.reset'='earliest';
CREATE STREAM product_supply_and_demand WITH (PARTITIONS=1, KAFKA_TOPIC='dc01_product_supply_and_demand') AS SELECT
  product_id,
  product_qty * -1 "QUANTITY"
FROM sales_enriched;
EOF

TOPICS_TO_CHECK=(
  "dc01_purchases_enriched"
  "dc01_product_supply_and_demand"
)

wait-for-schemas

docker-compose exec -T ksqldb-cli ksql http://ksqldb-server-ccloud:8088 << EOF
INSERT INTO product_supply_and_demand
  SELECT  product_id,
          product_qty "QUANTITY"
  FROM    purchases_enriched;
EOF

docker-compose exec -T ksqldb-cli ksql http://ksqldb-server-ccloud:8088 << EOF
SET 'auto.offset.reset'='earliest';
CREATE TABLE current_stock WITH (PARTITIONS = 1, KAFKA_TOPIC = 'dc01_current_stock') AS SELECT
      product_id
    , SUM(quantity) "STOCK_LEVEL"
FROM product_supply_and_demand
GROUP BY product_id;
EOF


docker-compose exec -T ksqldb-cli ksql http://ksqldb-server-ccloud:8088 << EOF
SET 'auto.offset.reset'='earliest';
CREATE TABLE product_demand_last_3mins_tbl WITH (PARTITIONS = 1, KAFKA_TOPIC = 'dc01_product_demand_last_3mins')
AS SELECT
      timestamptostring(windowStart,'HH:mm:ss') "WINDOW_START_TIME"
    , timestamptostring(windowEnd,'HH:mm:ss') "WINDOW_END_TIME"
    , product_id
    , SUM(product_qty) "DEMAND_LAST_3MINS"
FROM sales_enriched
WINDOW HOPPING (SIZE 3 MINUTES, ADVANCE BY 1 MINUTE)
GROUP BY product_id EMIT CHANGES;
EOF

TOPICS_TO_CHECK=(
  "dc01_product_demand_last_3mins"
)

wait-for-schemas

docker-compose exec -T ksqldb-cli ksql http://ksqldb-server-ccloud:8088 << EOF
CREATE STREAM product_demand_last_3mins WITH (KAFKA_TOPIC='dc01_product_demand_last_3mins', VALUE_FORMAT='AVRO');
EOF


TOPICS_TO_CHECK=(
  "dc01_product_demand_last_3mins"
  "dc01_current_stock"
)

wait-for-schemas


docker-compose exec -T ksqldb-cli ksql http://ksqldb-server-ccloud:8088 << EOF
SET 'auto.offset.reset' = 'latest';
CREATE STREAM out_of_stock_events WITH (PARTITIONS = 1, KAFKA_TOPIC = 'dc01_out_of_stock_events')
AS SELECT
  cs.product_id "PRODUCT_ID",
  pd.window_start_time,
  pd.window_end_time,
  cs.stock_level,
  pd.demand_last_3mins,
  (cs.stock_level * -1) + pd.DEMAND_LAST_3MINS "QUANTITY_TO_PURCHASE"
FROM product_demand_last_3mins pd
INNER JOIN current_stock cs ON pd.product_id = cs.product_id
WHERE stock_level <= 0;
EOF

curl -i -X POST -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:18083/connectors/ \
    -d '{
        "name": "replicator-ccloud-to-dc01",
        "config": {
          "connector.class": "io.confluent.connect.replicator.ReplicatorSourceConnector",
          "key.converter": "io.confluent.connect.replicator.util.ByteArrayConverter",
          "value.converter": "io.confluent.connect.replicator.util.ByteArrayConverter",
          "topic.config.sync": "false",
          "topic.whitelist": "dc01_out_of_stock_events",
          "dest.kafka.bootstrap.servers": "broker:29092",
          "dest.kafka.replication.factor": 1,
          "src.kafka.bootstrap.servers": "${file:/secrets.properties:CCLOUD_CLUSTER_ENDPOINT}",
          "src.kafka.security.protocol": "SASL_SSL",
          "src.kafka.sasl.mechanism": "PLAIN",
          "src.kafka.sasl.jaas.config": "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"${file:/secrets.properties:CCLOUD_API_KEY}\" password=\"${file:/secrets.properties:CCLOUD_API_SECRET}\";",
          "src.consumer.group.id": "replicator-ccloud-to-dc01",
          "src.consumer.interceptor.classes": "io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor",
          "src.consumer.confluent.monitoring.interceptor.bootstrap.servers": "${file:/secrets.properties:CCLOUD_CLUSTER_ENDPOINT}",
          "src.kafka.timestamps.producer.interceptor.classes": "io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor",
          "src.kafka.timestamps.producer.confluent.monitoring.interceptor.bootstrap.servers": "${file:/secrets.properties:CCLOUD_CLUSTER_ENDPOINT}",
          "tasks.max": "1"
        }
    }'

curl -i -X POST -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:18083/connectors/ \
    -d '{
        "name": "jdbc-mysql-sink",
        "config": {
          "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
          "topics": "dc01_out_of_stock_events",
          "connection.url": "jdbc:mysql://mysql:3306/orders",
          "connection.user": "mysqluser",
          "connection.password": "mysqlpw",
          "insert.mode": "INSERT",
          "batch.size": "3000",
          "auto.create": "true",
          "key.converter": "org.apache.kafka.connect.storage.StringConverter"
       }
    }'



curl -i -X POST -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:18084/connectors/ \
    -d '{
        "name": "dc01_mongodb_sink",
        "config": {
          "connector.class":"com.mongodb.kafka.connect.MongoSinkConnector",
          "tasks.max":"1",
          "topics":"dc01_sales_enriched,dc01_purchases_enriched",
          "connection.uri":"${file:/secrets.properties:MONGODBATLAS_SRV_ADDRESS}",
          "database":"demo",
          "collection":"dc01",
          "topic.override.dc01_sales_enriched.collection":"dc01_sales",
          "topic.override.dc01_purchases_enriched.collection":"dc01_purchases",
          "key.converter":"org.apache.kafka.connect.storage.StringConverter",
          "transforms":"WrapKey",
          "transforms.WrapKey.type":"org.apache.kafka.connect.transforms.HoistField$Key",
          "transforms.WrapKey.field":"ROWKEY",
          "document.id.strategy":"com.mongodb.kafka.connect.sink.processor.id.strategy.UuidStrategy",
          "post.processor.chain":"com.mongodb.kafka.connect.sink.processor.DocumentIdAdder",
          "max.batch.size":"20"
       }
    }'

#create topic 

curl -i -X POST -H "Accept:application/json" \
    -H  "Content-Type:application/json" http://localhost:18084/connectors/ \
    -d '{
        "name": "dc01_mongodb_source",
        "config": {
          "connector.class":"com.mongodb.kafka.connect.MongoSourceConnector",
          "tasks.max":1,
          "key.converter":"org.apache.kafka.connect.storage.StringConverter",
          "value.converter":"org.apache.kafka.connect.storage.StringConverter",
          "connection.uri":"${file:/secrets.properties:MONGODBATLAS_SRV_ADDRESS}",
          "database":"demo",
          "collection":"estore",
          "topic.prefix": "dc01_mdb"
       }
    }'

