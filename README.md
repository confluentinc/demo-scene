# demo-scene

Scripts and samples to support Confluent Platform talks. May be rough around the edges. For automated tutorials and QA'd code, see https://github.com/confluentinc/examples/

## Requirements

You need to allocate Docker 8GB when running these. Avoid allocating all your machine's cores to Docker as this may cause the machine to become unresponsive when running large stacks. On a four-core Mac Book two cores for Docker should be fine. 

## Contents

- Data pipelines (Database + KSQL + Elasticsearch)

  - [MySQL / Debezium CDC / KSQL / Elasticsearch](mysql-debezium-ksql-elasticsearch)
  - [Oracle CDC / KSQL / Elasticsearch](oracle-ksql-elasticsearch)
  - [Postgres / Debezium CDC / KSQL / Elasticsearch](postgres-debezium-ksql-elasticsearch)
  - [CDC demo with MySQL](no-more-silos-mysql)
  - [CDC demo with Oracle](no-more-silos-oracle)
  - [Building data pipelines with Confluent Cloud and GCP (BigQuery, GCS, etc)](gcp-pipeline)

- KSQL
  - [Live-Coding KSQL scripts](live-coding-ksql)
  - [KSQL UDF Advanced Example](ksql-udf-advanced-example)
  - [KSQL Troubleshooting](ksql-troubleshooting)
  - [ATM Fraud detection with Kafka and KSQL](ksql-atm-fraud-detection)
  - [Kafka Streams/KSQL Movie Demo](streams-movie-demo)
- Kafka Connect

  - [MQTT Connect Connector Demo](mqtt-connect-connector-demo)
  - [Kafka Connect deepdive](connect-deepdive) - understanding converters and serialization
  - [Kafka Connect JDBC Source demo environment](connect-jdbc)
  - [Example Kafka Connect syslog configuration and Docker Compose](syslog)

- Confluent Cloud

  - [Confluent Cloud "The Cube" Demo](ccloud-cube-demo)
  - [Connecting Confluent Cloud with Google Functions](connect-ccloud-google-functions)

- Misc
  - [Hacky export/import between Kafka clusters](export-import-with-kafkacat) using `kafkacat`
  - Docker Compose for just the [community licensed components of Confluent Platform](cos)
  - [Topic Tailer](topic-tailer), stream topics to the browser using websockets

## Feedback & Questions

Raise an issue on this github project, or head to http://cnfl.io/slack and join our Community Slack group.
