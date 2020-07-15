-- create a stream from data in `movies` topic 
create stream raw_movies with (kafka_topic='movies', partitions=1, value_format='avro');

-- test query and see data flowing
select movie_id, title, release_year, country, split(genres, '|') as genres, split(actors, '|') as actors, split(directors, '|') as directors, split(composers, '|') as composers, split(screenwriters, '|') as screenwriters, cinematographer, split(production_companies, '|') as production_companies  from raw_movies emit changes limit 1;

-- create a table of movies
create table movies_lookup as select movie_id, LATEST_BY_OFFSET(title), LATEST_BY_OFFSET(release_year), LATEST_BY_OFFSET(country)  from raw_movies group by movie_id emit changes;

-- query ksqlDB table by movie_id
select * from movies_lookup where movie_id=362;