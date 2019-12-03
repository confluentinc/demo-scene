/**********************************************/
/**************** REUSING DATA ****************/
/**********************************************/

SET 'auto.offset.reset' = 'earliest';

/**********************************************/
/************** INITIAL STREAMS ***************/
/**********************************************/

CREATE STREAM USER_GAME (USER VARCHAR, GAME STRUCT<SCORE INT, LIVES INT, LEVEL INT>)
WITH (KAFKA_TOPIC='USER_GAME', VALUE_FORMAT='JSON', PARTITIONS=6, REPLICAS=3);

CREATE STREAM USER_LOSSES (USER VARCHAR)
WITH (KAFKA_TOPIC='USER_LOSSES', VALUE_FORMAT='JSON', PARTITIONS=6, REPLICAS=3);

/**********************************************/
/***************** USER STATS *****************/
/**********************************************/

CREATE STREAM USER_GAME_REKEYED AS
	SELECT USER, GAME->SCORE AS SCORE, GAME->LIVES AS LIVES, GAME->LEVEL AS LEVEL 
	FROM USER_GAME PARTITION BY USER;

CREATE TABLE STATS_PER_USER AS
	SELECT USER AS USER, MAX(SCORE) AS HIGHEST_SCORE, MAX(LEVEL) AS HIGHEST_LEVEL
	FROM USER_GAME_REKEYED GROUP BY USER;

/**********************************************/
/**************** USER LOSSES *****************/
/**********************************************/

CREATE STREAM USER_LOSSES_REKEYED AS
	SELECT USER FROM USER_LOSSES
	PARTITION BY USER;

CREATE TABLE LOSSES_PER_USER AS
	SELECT USER, COUNT(USER) AS TOTAL_LOSSES
	FROM USER_LOSSES_REKEYED
	GROUP BY USER;

/**********************************************/
/***************** SCOREBOARD *****************/
/**********************************************/

CREATE TABLE SCOREBOARD AS
	SELECT S.USER AS USER, S.HIGHEST_SCORE AS HIGHEST_SCORE,
	S.HIGHEST_LEVEL AS HIGHEST_LEVEL, L.TOTAL_LOSSES AS TOTAL_LOSSES
	FROM STATS_PER_USER S LEFT JOIN LOSSES_PER_USER L ON S.USER = L.USER;
