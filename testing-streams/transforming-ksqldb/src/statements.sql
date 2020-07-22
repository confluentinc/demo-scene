CREATE STREAM raw_movies (ROWKEY INTEGER KEY, id INT, title VARCHAR, genre VARCHAR)
    WITH (kafka_topic='movies', value_format = 'avro');

CREATE STREAM movies WITH (kafka_topic = 'parsed_movies', partitions = 1) AS
    SELECT ROWKEY,
           id,
           split(title, '::')[1] as title,
           CAST(split(title, '::')[2] AS INT) AS year,
           genre
    FROM raw_movies;
