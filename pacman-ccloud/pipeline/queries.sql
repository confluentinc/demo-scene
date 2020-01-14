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
/***************** Scoreboard *****************/
/**********************************************/

CREATE TABLE SCOREBOARD AS
	SELECT
		UG.USER AS USER,
		MAX(UG.GAME->SCORE) AS HIGHEST_SCORE,
		MAX(UG.GAME->LEVEL) AS HIGHEST_LEVEL,
		COUNT(UL.USER) AS TOTAL_LOSSES
	FROM USER_GAME UG LEFT JOIN USER_LOSSES UL
	WITHIN 10 SECONDS ON UG.USER = UL.USER
	GROUP BY UG.USER;

/**********************************************/
/**************** Highest Score ***************/
/**********************************************/

CREATE STREAM HIGHEST_SCORE_SOURCE AS
	SELECT
		'HIGHEST_SCORE_KEY' AS HIGHEST_SCORE_KEY,
		GAME->SCORE AS SCORE
	FROM USER_GAME
	WHERE GAME->SCORE IS NOT NULL
	PARTITION BY HIGHEST_SCORE_KEY;

CREATE TABLE HIGHEST_SCORE AS
	SELECT
		HIGHEST_SCORE_KEY AS HIGHEST_SCORE_KEY,
		MAX(SCORE) AS HIGHEST_SCORE
	FROM HIGHEST_SCORE_SOURCE
	GROUP BY HIGHEST_SCORE_KEY;
