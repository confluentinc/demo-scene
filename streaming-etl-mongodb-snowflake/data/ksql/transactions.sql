CREATE STREAM transactions(
    ip VARCHAR,
    cust_id INT,
    prod_id INT,
    time VARCHAR)
WITH (
    KAFKA_TOPIC='abc-transactions', 
    VALUE_FORMAT='JSON'
);