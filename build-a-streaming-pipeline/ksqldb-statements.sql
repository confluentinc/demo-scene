CREATE SINK CONNECTOR SINK_ES_RATINGS WITH (
    'connector.class' = 'io.confluent.connect.elasticsearch.ElasticsearchSinkConnector',
    'topics'          = 'ratings',
    'connection.url'  = 'http://elasticsearch:9200',
    'type.name'       = '_doc',
    'key.ignore'      = 'false',
    'schema.ignore'   = 'true',
    'transforms'= 'ExtractTimestamp',
    'transforms.ExtractTimestamp.type'= 'org.apache.kafka.connect.transforms.InsertField$Value',
    'transforms.ExtractTimestamp.timestamp.field' = 'RATING_TS'
);

CREATE STREAM RATINGS_LIVE AS
SELECT USER_ID, STARS, CHANNEL, MESSAGE FROM RATINGS WHERE LCASE(CHANNEL) NOT LIKE '%test%' EMIT CHANGES;

CREATE STREAM RATINGS_TEST AS
SELECT USER_ID, STARS, CHANNEL, MESSAGE FROM RATINGS WHERE LCASE(CHANNEL) LIKE '%test%' EMIT CHANGES;

CREATE SOURCE CONNECTOR SOURCE_MYSQL_01 WITH (
    'connector.class' = 'io.debezium.connector.mysql.MySqlConnector',
    'database.hostname' = 'mysql',
    'database.port' = '3306',
    'database.user' = 'debezium',
    'database.password' = 'dbz',
    'database.server.id' = '42',
    'database.server.name' = 'asgard',
    'table.whitelist' = 'demo.customers',
    'database.history.kafka.bootstrap.servers' = 'kafka:29092',
    'database.history.kafka.topic' = 'dbhistory.demo' ,
    'include.schema.changes' = 'false',
    'transforms'= 'unwrap,extractkey',
    'transforms.unwrap.type'= 'io.debezium.transforms.ExtractNewRecordState',
    'transforms.extractkey.type'= 'org.apache.kafka.connect.transforms.ExtractField$Key',
    'transforms.extractkey.field'= 'id',
    'key.converter'= 'org.apache.kafka.connect.storage.StringConverter',
    'value.converter'= 'io.confluent.connect.avro.AvroConverter',
    'value.converter.schema.registry.url'= 'http://schema-registry:8081'
    );

-- Need to wait here for the topic to be created

CREATE TABLE CUSTOMERS (CUSTOMER_ID VARCHAR PRIMARY KEY) 
  WITH (KAFKA_TOPIC='asgard.demo.CUSTOMERS', VALUE_FORMAT='AVRO');

CREATE STREAM CUSTOMERS_STREAM (CUSTOMER_ID VARCHAR KEY) WITH (KAFKA_TOPIC='asgard.demo.CUSTOMERS', VALUE_FORMAT='AVRO');



SET 'auto.offset.reset' = 'earliest';
CREATE STREAM RATINGS_WITH_CUSTOMER_DATA 
       WITH (KAFKA_TOPIC='ratings-enriched') 
       AS 
SELECT R.RATING_ID, R.MESSAGE, R.STARS, R.CHANNEL,
       C.CUSTOMER_ID, C.FIRST_NAME + ' ' + C.LAST_NAME AS FULL_NAME, 
       C.CLUB_STATUS, C.EMAIL 
FROM   RATINGS R 
       LEFT JOIN CUSTOMERS C 
         ON CAST(R.USER_ID AS STRING) = C.CUSTOMER_ID      
WHERE  C.FIRST_NAME IS NOT NULL
EMIT CHANGES;


CREATE STREAM UNHAPPY_PLATINUM_CUSTOMERS AS 
SELECT FULL_NAME, CLUB_STATUS, EMAIL, STARS, MESSAGE 
FROM   RATINGS_WITH_CUSTOMER_DATA 
WHERE  STARS < 3 
  AND  CLUB_STATUS = 'platinum'
PARTITION BY FULL_NAME;

CREATE SINK CONNECTOR SINK_ELASTIC_01 WITH (
  'connector.class' = 'io.confluent.connect.elasticsearch.ElasticsearchSinkConnector',
  'connection.url' = 'http://elasticsearch:9200',
  'type.name' = '',
  'behavior.on.malformed.documents' = 'warn',
  'errors.tolerance' = 'all',
  'errors.log.enable' = 'true',
  'errors.log.include.messages' = 'true',
  'topics' = 'ratings-enriched,UNHAPPY_PLATINUM_CUSTOMERS',
  'key.ignore' = 'true',
  'schema.ignore' = 'true',
  'key.converter' = 'org.apache.kafka.connect.storage.StringConverter',
  'transforms'= 'ExtractTimestamp',
  'transforms.ExtractTimestamp.type'= 'org.apache.kafka.connect.transforms.InsertField$Value',
  'transforms.ExtractTimestamp.timestamp.field' = 'EXTRACT_TS'
);


CREATE TABLE RATINGS_PER_CUSTOMER_PER_15MINUTE AS 
SELECT FULL_NAME,COUNT(*) AS RATINGS_COUNT, COLLECT_LIST(STARS) AS RATINGS
  FROM RATINGS_WITH_CUSTOMER_DATA 
        WINDOW TUMBLING (SIZE 15 MINUTE) 
  GROUP BY FULL_NAME
  EMIT CHANGES;


SELECT TIMESTAMPTOSTRING(WINDOWSTART, 'yyyy-MM-dd HH:mm:ss') AS WINDOW_START_TS, 
       FULL_NAME, 
       RATINGS_COUNT, 
       RATINGS
  FROM RATINGS_PER_CUSTOMER_PER_15MINUTE 
  WHERE FULL_NAME='Rica Blaisdell'
  EMIT CHANGES;

SELECT TIMESTAMPTOSTRING(WINDOWSTART, 'yyyy-MM-dd HH:mm:ss') AS WINDOW_START_TS, 
       FULL_NAME,
       RATINGS_COUNT, 
       RATINGS
FROM   RATINGS_PER_CUSTOMER_PER_15MINUTE
WHERE  FULL_NAME='Rica Blaisdell'
  AND  WINDOWSTART > '2020-07-06T15:30:00.000';
