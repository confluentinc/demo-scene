/**********************************************/
/*                                            */
/*  Before running this script, it is a good  */
/*  idea to set the position of the offset to */
/*  the earliest position to load all data.   */
/*                                            */
/**********************************************/
/*                                            */
/*   SET 'auto.offset.reset' = 'earliest';    */
/*                                            */
/**********************************************/

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

CREATE TABLE SCOREBOARD AS
	SELECT
		SPU.USER AS USER,
		SPU.HIGHEST_SCORE AS HIGHEST_SCORE,
		SPU.HIGHEST_LEVEL AS HIGHEST_LEVEL,
		CASE WHEN LPU.TOTAL_LOSSES IS NULL
			THEN CAST(0 AS BIGINT)
			ELSE LPU.TOTAL_LOSSES
		END AS TOTAL_LOSSES
	FROM STATS_PER_USER SPU LEFT JOIN
	LOSSES_PER_USER LPU ON SPU.USER = LPU.USER;

/**********************************************/
/**************** Highest Score ***************/
/**********************************************/

CREATE TABLE HIGHEST_SCORE AS
	SELECT
		MAX(GAME->SCORE) AS HIGHEST_SCORE
	FROM USER_GAME GROUP BY 'HIGHEST_SCORE';
