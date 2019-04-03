-- ###################################################
-- These are the commands used during the workshop. 
-- You can use this file to catch up to certain stages
-- of the workshop if you want.
-- ###################################################

SET 'auto.offset.reset' = 'earliest';

CREATE STREAM RATINGS WITH (KAFKA_TOPIC='ratings', VALUE_FORMAT='AVRO');

CREATE STREAM POOR_RATINGS AS SELECT * FROM ratings WHERE STARS <3 AND CHANNEL='iOS';

CREATE STREAM CUSTOMERS_SRC WITH (KAFKA_TOPIC='asgard.demo.CUSTOMERS', VALUE_FORMAT='AVRO');

CREATE STREAM CUSTOMERS_SRC_REKEY \
        WITH (PARTITIONS=1) AS \
        SELECT * FROM CUSTOMERS_SRC PARTITION BY ID;

-- This SELECT is just a way to get KSQL to wait until a message has passed through it.
-- If it doesn't then the subsequent CREATE TABLE will fail because they schema won't 
-- be registered for the topic yet
SELECT * FROM CUSTOMERS_SRC_REKEY LIMIT 1;
CREATE TABLE CUSTOMERS WITH (KAFKA_TOPIC='CUSTOMERS_SRC_REKEY', VALUE_FORMAT ='AVRO', KEY='ID');

CREATE STREAM RATINGS_WITH_CUSTOMER_DATA WITH (PARTITIONS=1) AS \
SELECT R.RATING_ID, R.CHANNEL, R.STARS, R.MESSAGE, \
       C.ID, C.CLUB_STATUS, C.EMAIL, \
       C.FIRST_NAME, C.LAST_NAME \
FROM RATINGS R \
     INNER JOIN CUSTOMERS C \
       ON R.USER_ID = C.ID;

CREATE STREAM UNHAPPY_PLATINUM_CUSTOMERS AS \
SELECT CLUB_STATUS, EMAIL, STARS, MESSAGE \
FROM   RATINGS_WITH_CUSTOMER_DATA \
WHERE  STARS < 3 \
  AND  CLUB_STATUS = 'platinum';

CREATE TABLE RATINGS_BY_CLUB_STATUS AS \
SELECT WindowStart() AS WINDOW_START_TS, CLUB_STATUS, COUNT(*) AS RATING_COUNT \
FROM RATINGS_WITH_CUSTOMER_DATA \
     WINDOW TUMBLING (SIZE 1 MINUTES) \
GROUP BY CLUB_STATUS;
