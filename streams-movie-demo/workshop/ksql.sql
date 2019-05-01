
-- -----------------------------------------------------------------------------------

head -n1 ratings-json.js | kafkacat -b localhost:9092 -t ratings -P
head -n1 movies-json.js  | kafkacat -b localhost:9092 -t movies -P

SET 'auto.offset.reset' = 'earliest';


CREATE STREAM movies_src (movie_id BIGINT, title VARCHAR, release_year INT, country VARCHAR, rating DOUBLE, cinematographer VARCHAR, genres ARRAY<VARCHAR>, directors ARRAY<VARCHAR>, composers ARRAY<varchar>, screenwriters ARRAY<VARCHAR>, production_companies ARRAY<VARCHAR>) WITH (VALUE_FORMAT='JSON', KAFKA_TOPIC='movies');

CREATE STREAM movies_rekeyed WITH (PARTITIONS=1) AS SELECT * FROM movies_src PARTITION BY movie_id;

kafkacat -C  -K: -b localhost:9092 -f 'Key:    %k\nValue:  %s\n' -t movies
kafkacat -C  -K: -b localhost:9092 -f 'Key:    %k\nValue:  %s\n' -t MOVIES_REKEYED

CREATE TABLE movies_ref (movie_id BIGINT, title VARCHAR, release_year INT, country VARCHAR, rating DOUBLE, cinematographer VARCHAR, genres ARRAY<VARCHAR>, directors ARRAY<VARCHAR>, composers ARRAY<varchar>, screenwriters ARRAY<VARCHAR>, production_companies ARRAY<VARCHAR>) WITH (VALUE_FORMAT='JSON', KAFKA_TOPIC='MOVIES_REKEYED', KEY='movie_id');

--CREATE TABLE movies_ref  WITH (VALUE_FORMAT='AVRO', KAFKA_TOPIC='MOVIES', KEY='movie_id');

CREATE STREAM ratings (movie_id BIGINT, rating DOUBLE) WITH (VALUE_FORMAT = 'JSON', KAFKA_TOPIC='ratings');

cat movies-json.js | kafkacat -b localhost:9092 -t movies -P

SELECT m.title, m.release_year, r.rating FROM ratings r LEFT JOIN movies_ref m on r.movie_id = m.movie_id;

head -n1000 ratings-json.js | kafkacat -b localhost:9092 -t ratings -P

CREATE TABLE movie_ratings AS SELECT m.title, SUM(r.rating)/COUNT(r.rating) AS avg_rating, COUNT(r.rating) AS num_ratings FROM ratings r LEFT JOIN movies_ref m ON m.movie_id = r.movie_id GROUP BY m.title;


CREATE TABLE popular_movies AS SELECT m.title, SUM(r.rating)/COUNT(r.rating) AS avg_rating, COUNT(r.rating) AS num_ratings FROM ratings r LEFT JOIN movies_ref m ON m.movie_id = r.movie_id GROUP BY m.title HAVING SUM(r.rating)/COUNT(r.rating) > 8;


SELECT ROWKEY, ROWTIME, title, avg_rating FROM movie_ratings;



-- add user table with geography and gender
-- then add user_id to ratings
-- then we can see whether men or women like Tarantino better



CREATE STREAM movies_src (movie_id BIGINT, title VARCHAR, release_year INT, country VARCHAR, rating DOUBLE, cinematographer VARCHAR, genres ARRAY<VARCHAR>, directors ARRAY<VARCHAR>, composers ARRAY<varchar>, screenwriters ARRAY<VARCHAR>, production_companies ARRAY<VARCHAR>) WITH (VALUE_FORMAT='JSON', KAFKA_TOPIC='movies');

CREATE STREAM movies_rekeyed AS SELECT * FROM movies_src PARTITION BY movie_id;

CREATE TABLE movies_ref (movie_id BIGINT, title VARCHAR, release_year INT, country VARCHAR, rating DOUBLE, cinematographer VARCHAR, genres ARRAY<VARCHAR>, directors ARRAY<VARCHAR>, composers ARRAY<varchar>, screenwriters ARRAY<VARCHAR>, production_companies ARRAY<VARCHAR>) WITH (VALUE_FORMAT='JSON', KAFKA_TOPIC='MOVIES_REKEYED');

CREATE STREAM ratings (movie_id BIGINT, rating DOUBLE) WITH (VALUE_FORMAT = 'JSON', KAFKA_TOPIC='ratings');

CREATE TABLE movie_ratings AS SELECT m.title, SUM(r.rating)/COUNT(r.rating) AS avg_rating, COUNT(r.rating) AS num_ratings FROM ratings r LEFT OUTER JOIN movies_ref m ON m.movie_id = r.movie_id GROUP BY m.title;









-------------

docker-compose up -d
docker-compose logs -f control-center | grep -e HTTP
scripts/01-start-twitter-connector.sh
scripts/consume-twitter-feed.sh
scripts/02_start-ksql-server-with-props-and-commands.sh
scripts/start-ksql-cli.sh	