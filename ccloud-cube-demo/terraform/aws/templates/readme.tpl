################################ Retrieving the Schema ###############################

curl -X GET ${schema_registry_endpoint}/schemas/ids/1

################################### Creating Topics ##################################

ccloud topic create _NUMBERS --partitions 4 --replication-factor 3

ccloud topic create _EVENTS --partitions 4 --replication-factor 3

################################### Loading Numbers ##################################

kafka-console-consumer --bootstrap-server ${broker_list} --consumer.config ccloud.properties --topic _NUMBERS --property "print.key=true" --from-beginning

kafka-console-producer --broker-list ${broker_list} --producer.config ccloud.properties --topic _NUMBERS --property "parse.key=true" --property "key.separator=:"

1:{"NUMBER" : 1, "X": 1, "Y" : 0, "Z" : 0}

2:{"NUMBER" : 2, "X": 1, "Y" : -90, "Z" : 1}

3:{"NUMBER" : 3, "X": -180, "Y" : 0, "Z" : 180}

4:{"NUMBER" : 4, "X": 1, "Y" : 90, "Z" : -1}

############################### Accessing the KSQL CLI ###############################

ksql ${ksql_server_endpoint}

LIST TOPICS;

PRINT _NUMBERS FROM BEGINNING;

PRINT _EVENTS FROM BEGINNING;

################################## Creating Tables ###################################

CREATE TABLE NUMBERS (NUMBER BIGINT, X INTEGER, Y INTEGER, Z INTEGER) WITH (KAFKA_TOPIC='_NUMBERS', VALUE_FORMAT='JSON', KEY='NUMBER');

DESCRIBE NUMBERS;

SET 'auto.offset.reset' = 'earliest';

SELECT * FROM NUMBERS;

################################## Creating Streams ##################################

CREATE STREAM EVENTS WITH (KAFKA_TOPIC='_EVENTS', VALUE_FORMAT='AVRO');

DESCRIBE EVENTS;

SET 'auto.offset.reset' = 'latest';

SELECT NAME, MOTION->X, MOTION->Y, MOTION->Z FROM EVENTS;

################################## Playing the Game ##################################

CREATE STREAM EVENTS_ENRICHED AS SELECT NAME, MOTION->X AS X, MOTION->Y AS Y, MOTION->Z AS Z, 3 AS NUMBER FROM EVENTS;

CREATE TABLE SELECTED_WINNERS AS SELECT E.NAME AS NAME, COUNT(*) AS TOTAL FROM EVENTS_ENRICHED E LEFT OUTER JOIN NUMBERS N ON E.NUMBER = N.NUMBER WHERE E.X = N.X AND E.Y = N.Y AND E.Z = N.Z GROUP BY NAME;

SELECT CONCAT('AND THE WINNER IS ----------> ', NAME) AS MESSAGE FROM SELECTED_WINNERS;

#######################################################################################