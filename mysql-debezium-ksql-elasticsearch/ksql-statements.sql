-- Process all data that currently exists in topic, as well as future data
SET 'auto.offset.reset' = 'earliest';

-- Declare source stream
CREATE STREAM CUSTOMERS_SRC WITH (KAFKA_TOPIC='asgard.demo.CUSTOMERS', VALUE_FORMAT='AVRO');

-- Re-partition on the ID column and set the target topic to
-- match the same number of partitings as the source ratings topic:
CREATE STREAM CUSTOMERS_SRC_REKEY WITH (PARTITIONS=1) AS SELECT * FROM CUSTOMERS_SRC PARTITION BY ID;

-- Register the CUSTOMER data as a KSQL table, sourced from the re-partitioned topic
CREATE TABLE CUSTOMERS WITH (KAFKA_TOPIC='CUSTOMERS_SRC_REKEY', VALUE_FORMAT ='AVRO', KEY='ID');

-- Register the RATINGS data as a KSQL stream, sourced from the ratings topic
CREATE STREAM RATINGS WITH (KAFKA_TOPIC='ratings',VALUE_FORMAT='AVRO');

-- Perform the join, writing to a new topic.
CREATE STREAM RATINGS_WITH_CUSTOMER_DATA \
       WITH (PARTITIONS=1, \
             KAFKA_TOPIC='ratings-enriched') \
       AS \
SELECT R.RATING_ID, R.MESSAGE, R.STARS, R.CHANNEL,\
      C.ID, C.FIRST_NAME + ' ' + C.LAST_NAME AS FULL_NAME, \
      C.CLUB_STATUS, C.EMAIL \
      FROM RATINGS R \
        LEFT JOIN CUSTOMERS C \
        ON R.USER_ID = C.ID \
      WHERE C.FIRST_NAME IS NOT NULL;


CREATE STREAM UNHAPPY_PLATINUM_CUSTOMERS \
       WITH (VALUE_FORMAT='JSON', PARTITIONS=1) AS \
SELECT FULL_NAME, CLUB_STATUS, EMAIL, STARS, MESSAGE \
FROM   RATINGS_WITH_CUSTOMER_DATA \
WHERE  STARS < 3 \
  AND  CLUB_STATUS = 'platinum';
