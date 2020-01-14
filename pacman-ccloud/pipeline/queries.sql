/**********************************************/
/**************** REUSING DATA ****************/
/**********************************************/

SET 'auto.offset.reset' = 'earliest';

/**********************************************/
/*************** Stream Sources ***************/
/**********************************************/

CREATE STREAM USER_GAME (USER VARCHAR, GAME STRUCT<SCORE INT, LIVES INT, LEVEL INT>)
WITH (KAFKA_TOPIC='USER_GAME', VALUE_FORMAT='JSON', PARTITIONS=6, REPLICAS=3);

CREATE STREAM USER_LOSSES (USER VARCHAR)
WITH (KAFKA_TOPIC='USER_LOSSES', VALUE_FORMAT='JSON', PARTITIONS=6, REPLICAS=3);

/**********************************************/
/************** Computed Tables ***************/
/**********************************************/

CREATE TABLE STATS_PER_USER AS
	SELECT
		USER AS USER,
		MAX(GAME->SCORE) AS HIGHEST_SCORE,
		MAX(GAME->LEVEL) AS HIGHEST_LEVEL
	FROM USER_GAME
	GROUP BY USER;

CREATE TABLE LOSSES_PER_USER AS
	SELECT
		USER AS USER,
		COUNT(USER) AS TOTAL_LOSSES
	FROM USER_LOSSES
	GROUP BY USER;

/**********************************************/
/***************** Scoreboard *****************/
/**********************************************/

CREATE TABLE SCOREBOARD AS
	SELECT
		S.USER AS USER,
		S.HIGHEST_SCORE AS HIGHEST_SCORE,
		S.HIGHEST_LEVEL AS HIGHEST_LEVEL,
		L.TOTAL_LOSSES AS TOTAL_LOSSES
	FROM
		STATS_PER_USER S LEFT JOIN
		LOSSES_PER_USER L ON S.USER = L.USER;

/**********************************************/
/**************** Highest Score ***************/
/**********************************************/

CREATE STREAM HIGHEST_SCORE_SOURCE AS
	SELECT
		'HIGHEST_SCORE_KEY' AS HIGHEST_SCORE_KEY,
		GAME->SCORE AS SCORE
	FROM USER_GAME;

CREATE TABLE HIGHEST_SCORE AS
	SELECT
		HIGHEST_SCORE_KEY AS HIGHEST_SCORE_KEY,
		MAX(SCORE) AS HIGHEST_SCORE
	FROM HIGHEST_SCORE_SOURCE
	GROUP BY HIGHEST_SCORE_KEY;
