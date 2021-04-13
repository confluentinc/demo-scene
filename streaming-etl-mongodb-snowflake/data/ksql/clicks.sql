CREATE STREAM clicks(
    ip VARCHAR,
    userid INT,
    prod_id INT,
    time VARCHAR,
    _time BIGINT,
    bytes BIGINT,
    referrer VARCHAR,
    agent VARCHAR)
WITH (
    KAFKA_TOPIC='abc-clicks', 
    VALUE_FORMAT='JSON'
);