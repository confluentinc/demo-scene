CREATE STREAM RATINGS WITH (KAFKA_TOPIC='ratings',VALUE_FORMAT='AVRO');

CREATE STREAM POOR_RATINGS AS SELECT STARS, CHANNEL, MESSAGE FROM RATINGS WHERE STARS<3;

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

CREATE TABLE  CUSTOMERS WITH (KAFKA_TOPIC='asgard.demo.CUSTOMERS', VALUE_FORMAT='AVRO');

SET 'auto.offset.reset' = 'earliest';
CREATE STREAM RATINGS_WITH_CUSTOMER_DATA 
       WITH (KAFKA_TOPIC='ratings-enriched') 
       AS 
SELECT R.RATING_ID, R.MESSAGE, R.STARS, R.CHANNEL,
       C.ID, C.FIRST_NAME + ' ' + C.LAST_NAME AS FULL_NAME, 
       C.CLUB_STATUS, C.EMAIL 
FROM   RATINGS R 
       LEFT JOIN CUSTOMERS C 
         ON CAST(R.USER_ID AS STRING) = C.ROWKEY      
WHERE  C.FIRST_NAME IS NOT NULL
EMIT CHANGES;


CREATE STREAM UNHAPPY_PLATINUM_CUSTOMERS AS 
SELECT FULL_NAME, CLUB_STATUS, EMAIL, STARS, MESSAGE 
FROM   RATINGS_WITH_CUSTOMER_DATA 
WHERE  STARS < 3 
  AND  CLUB_STATUS = 'platinum';


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


CREATE TABLE RATINGS_PER_CUSTOMER_PER_MINUTE AS 
SELECT FULL_NAME,COUNT(*) AS RATINGS_COUNT
  FROM RATINGS_WITH_CUSTOMER_DATA 
        WINDOW TUMBLING (SIZE 1 MINUTE) 
  GROUP BY FULL_NAME
  EMIT CHANGES;


SELECT TIMESTAMPTOSTRING(WINDOWSTART, 'yyyy-MM-dd HH:mm:ss') AS WINDOW_START_TS, 
       FULL_NAME, 
       RATINGS_COUNT 
  FROM RATINGS_PER_CUSTOMER_PER_MINUTE 
  WHERE ROWKEY='Rica Blaisdell'
  EMIT CHANGES;

SELECT TIMESTAMPTOSTRING(WINDOWSTART, 'yyyy-MM-dd HH:mm:ss') AS WINDOW_START_TS, 
       FULL_NAME,
       RATINGS_COUNT
FROM   RATINGS_PER_CUSTOMER_PER_MINUTE
WHERE  ROWKEY='Rica Blaisdell'
  AND  WINDOWSTART > '2020-03-04T12:00:00.000';


# Store the epoch (milliseconds) five minutes ago
PREDICATE=$(date --date '-5 min' +%s)000

# Pull from ksqlDB the aggregate-by-minute for the last five minutes for a given user: 
curl -X "POST" "http://ksqldb:8088/query" \
     -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
     -d '{"ksql":"SELECT TIMESTAMPTOSTRING(WINDOWSTART, '\''yyyy-MM-dd HH:mm:ss'\'') AS WINDOW_START_TS,        FULL_NAME,       RATINGS_COUNT FROM   RATINGS_PER_CUSTOMER_PER_MINUTE WHERE  ROWKEY='\''Rica Blaisdell'\''   AND  WINDOWSTART > '$PREDICATE';"}'
