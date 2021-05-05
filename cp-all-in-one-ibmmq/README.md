![image](../images/confluent-logo-300-2.png)

# IBM MQ demo

This repository demonstrates how to use the IBM MQ connector. Two connectors will be started up: Datagen source, to mock clickstream data and IBM MQ Connetor source. Then we'll use KSQL to join the two sources together. No sink connector is configured.

## Make commands

```bash
make build
make cluster
# wait a minute for cluster to spinup
```

## Make the topics

```bash
make topic
# wait a minute before moving on to the next step
```

## Open the IBM MQ Dashboard

[log in](https://localhost:9443/ibmmq/console/login.html)

```conf
UserName=admin
Password=passw0rd
```

## Show AVRO schema in C3 topics

You need to send a message to IBM MQ before the schema will appear in the topic in C3.

- Select `DEV.QUEUE.1` under "Queues on MQ1"

![ibmmq](images/ibmmq-queues.png)

- Add a message

![add image](images/addmessage.png)
![add image](images/addmessage2.png)

- You can now see the schema assigned to the `ibmmq` topic

![ibmmq topic](images/ibmmq-schema.png)

## Make the source connectors

```bash
make connectsource
# wait a minute before moving on to the next step
```

## AVRO message appear in consumer

Run the ibmmq consumer to see messages coming in from `DEV.QUEUE.1` (or check in C3)

```bash
make consumer
```

## KSQL

### Create the stream from the CLICKSTREAM topic with ksqlDB

```sql
CREATE STREAM CLICKSTREAM
  WITH (KAFKA_TOPIC='clickstream',
        VALUE_FORMAT='AVRO');
```

## Add anothermessage to DEV.QUEUE.1

You can use the user names `bobk_43` or `akatz1022` to capture clickstreams for those users with a KSQL join.

## Create the Stream for the IBMMQ topic

This time we will use KSQL to create the stream. Paste the KSQL statement into the KSQL Editor.

```sql
CREATE STREAM ibmmq
  WITH (KAFKA_TOPIC='ibmmq',
        VALUE_FORMAT='AVRO');
```

```sql
SELECT * FROM ibmmq
EMIT CHANGES;
```

```sql
SELECT "TEXT" FROM ibmmq
EMIT CHANGES;
```

## JOIN the 2 streams

Paste the KSQL statement into the KSQL Editor to perform the join.

```sql
CREATE STREAM VIP_USERS AS
select  * from  CLICKSTREAM
join  IBMMQ WITHIN 5 seconds
on text = username emit changes;
```

![join](images/join.png)

```sql
SELECT * FROM VIP_USERS 
emit changes;
```

## Configure DB2

```bash
docker exec -ti ibmdb2 bash -c "su - db2inst1"
```

```bash
db2 connect to sample user db2inst1 using passw0rd
```

```bash
db2 LIST TABLES
```

Now you can create the connector to load the data from db2

```bash
make connectdb2source
```

## Sink data to IBM MQ 

Let's sink the new stream data into IBM MQ into `DEV.QUEUE.2`

```bash
make connectsink
```

You can see the data by [loggin in](https://localhost:9443/ibmmq/console/login.html)

```conf
UserName=admin
Password=passw0rd
```
