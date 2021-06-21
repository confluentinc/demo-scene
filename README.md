![`demo-scene`](images/cover.png)

Scripts and samples to support Confluent Platform talks. May be rough around the edges. For automated tutorials and QA'd code, see https://github.com/confluentinc/examples/

## Requirements

You may well need to allocate Docker 8GB when running these. Avoid allocating all your machine's cores to Docker as this may cause the machine to become unresponsive when running large stacks. On a four-core Mac Book two cores for Docker should be fine. 

## Contents

### Livestreams

[🎥 recordings](https://www.youtube.com/playlist?list=PLa7VYi0yPIH26-ppF0Gcsx-YVQQbNjDEt)

- [Code](livestreams)

###  Applications

- [Building a Telegram bot with Go, Apache Kafka, and ksqlDB](telegram-bot-carparks) ([🎥 talk](https://rmoff.dev/carpark-telegram-bot))
- [Streaming Pac-Man](streaming-pacman)
- [Workshop: Event Driven Microservices](event-driven-microservices-workshop)
- [Workshop: Choosing Christmas Movies with Kubernetes, Spring Boot, and Apache Kafka](streaming-movies-workshop) ([🎥 recording](https://www.youtube.com/watch?v=ampmQg33JtY))
- [Kafka Summit NYC: Confluent Operator Demo aka Escape from EKS](ksummit-operator-demo)
- [Kafka Summit 2020 - I don’t always test my streams, but when I do, I do it in production!](testing-streams)
- [Apache Kafka® Event Streaming Platform For Kotlin Developers](wordcount-spring-cloud-stream-kotlin) ([🎥 recording](https://youtu.be/Y-sqGKsnSHI))
  
###  Data pipelines (Database + KSQL + Elasticsearch)

- [Streaming ETL and Analytics on Confluent with Maritime AIS data](maritime-ais)
- [Pipeline to the cloud](pipeline-to-the-cloud) - on-premises RDBMS to Cloud datawarehouse e.g. Snowflake ([✍️ blog](https://www.confluent.io/blog/cloud-analytics-for-on-premises-data-streams-with-kafka/?utm_campaign=rmoff&utm_source=demo-scene))
- [Rail data streaming pipeline](rail-data-streaming-pipeline) ([🗣️talk](https://rmoff.dev/oredev19-on-track-with-kafka))
- [Apache Kafka and KSQL in Action: Let’s Build a Streaming Data Pipeline!](build-a-streaming-pipeline) ([🗣️talk](https://go.rmoff.net/devoxxuk19))
- [MySQL / Debezium CDC / KSQL / Elasticsearch](mysql-debezium-ksql-elasticsearch)
- [Oracle CDC / KSQL / Elasticsearch](oracle-ksql-elasticsearch)
- [Postgres / Debezium CDC / KSQL / Elasticsearch](postgres-debezium-ksql-elasticsearch)
- [CDC demo with MySQL](no-more-silos) ([🗣️talk](http://rmoff.dev/ksny19-no-more-silos))
- [CDC demo with Oracle](no-more-silos-oracle)
- [Building data pipelines with Confluent Cloud and GCP (BigQuery, GCS, etc)](gcp-pipeline)
- [MS SQL with Debezium and ksqlDB](mssql-to-kafka-with-ksqldb) ([✍️ blog](https://rmoff.net/2020/09/18/using-the-debezium-ms-sql-connector-with-ksqldb-embedded-kafka-connect/))
- [Streaming ETL pipeline from MongoDB to Snowflake with Apache Kafka®](streaming-etl-mongodb-snowflake)
- [Bridge to Cloud (and back!) with Confluent and MongoDB Atlas](mongodb-demo)

###  ksqlDB (previously known as KSQL)
- [Introduction to ksqlDB 01](introduction-to-ksqldb) ([🗣️talk](https://rmoff.dev/ksqldb-slides))
- [Introduction to KSQL   02](ksql-intro)
- [ksqlDB tombstones](ksqldb-tombstones)
- [Using Twitter data with ksqlDB](ksqldb-twitter)
- [KSQL UDF Advanced Example](ksql-udf-advanced-example)
- [KSQL Troubleshooting](ksql-troubleshooting)
- [ATM Fraud detection with Kafka and KSQL](ksql-atm-fraud-detection) ([🗣️talk](https://talks.rmoff.net/Cw9hbI/atm-fraud-detection-with-apache-kafka-and-ksql))
- [Kafka Streams/KSQL Movie Demo](streams-movie-demo)
- [KSQL MQTT demo](mqtt-tracker)
- [KSQL Dump Utility](ksql-dump)
- [KSQL workshop](ksql-workshop) (more recent version is [here](build-a-streaming-pipeline/workshop/))
- [Multi-node ksqlDB and Kafka Connect clusters](multi-cluster-connect-and-ksql)
- [Streaming ETL pipeline from MongoDB to Snowflake with Apache Kafka®](streaming-etl-mongodb-snowflake)
- [Bridge to Cloud (and back!) with Confluent and MongoDB Atlas](mongodb-demo)

###  Kafka Connect

- [Single Message Transform in Kafka Connect](kafka-connect-single-message-transforms)
- [Improvements Kafka Connect in Apache Kafka 2.3](connect-5.3-improvements)
- [From Zero to Hero with Kafka Connect](kafka-connect-zero-to-hero) ([🗣️talk](https://rmoff.dev/crunch19-zero-to-hero-kafka-connect))
- [Kafka Connect Converters and Serialization](connect-deepdive) ([✍️ blog](https://www.confluent.io/blog/kafka-connect-deep-dive-converters-serialization-explained/?utm_campaign=rmoff&utm_source=demo-scene))
- [Building a Kafka Connect cluster](connect-cluster)
- [Kafka Connect error handling](connect-error-handling) ([✍️ blog](https://www.confluent.io/blog/kafka-connect-deep-dive-error-handling-dead-letter-queues/?utm_campaign=rmoff&utm_source=demo-scene))
- [Multi-node ksqlDB and Kafka Connect clusters](multi-cluster-connect-and-ksql)
- Specific connectors
    - [ 👉 S3 Sink](kafka-to-s3) (🎥 [tutorial](https://rmoff.dev/kafka-s3-video))
    - [ 👉 Database](kafka-to-database) (tutorial [🎥 1](https://rmoff.dev/kafka-jdbc-video) / [🎥 2](https://rmoff.dev/ksqldb-jdbc-sink-video))
    - [ 👉 Elasticsearch](kafka-to-elasticsearch) (🎥 [Tutorial](https://rmoff.dev/kafka-elasticsearch-video))
    - [RSS feed into Kafka](rss-feed-into-kafka)
    - [Loading CSV data into Kafka](csv-to-kafka) ([🎥 Tutorial](https://rmoff.dev/csv-into-kafka))
    - [Loading XML data into Kafka](xml-to-kafka)
    - [Kafka Connect JDBC Source demo environment](connect-jdbc) ([✍️ blog](https://www.confluent.io/blog/kafka-connect-deep-dive-jdbc-source-connector/?utm_campaign=rmoff&utm_source=demo-scene))
    - [InfluxDB & Kafka Connect](influxdb-and-kafka) ([✍️ blog](https://rmoff.net/2020/01/23/notes-on-getting-data-into-influxdb-from-kafka-with-kafka-connect/?utm_campaign=rmoff&utm_source=demo-scene))
    - [RabbitMQ into Kafka](rabbitmq-into-kafka) ([✍️ blog](https://rmoff.net/2020/01/08/streaming-messages-from-rabbitmq-into-kafka-with-kafka-connect/?utm_campaign=rmoff&utm_source=demo-scene))
    - [MQTT Connect Connector Demo](mqtt-connect-connector-demo)
    - [Example Kafka Connect syslog configuration and Docker Compose](syslog) (see blog series [1](https://www.confluent.io/blog/real-time-syslog-processing-apache-kafka-ksql-part-1-filtering/?utm_campaign=rmoff&utm_source=demo-scene)/[2](https://www.confluent.io/blog/real-time-syslog-processing-with-apache-kafka-and-ksql-part-2-event-driven-alerting-with-slack/?utm_campaign=rmoff&utm_source=demo-scene)/[3](https://www.confluent.io/blog/real-time-syslog-processing-apache-kafka-ksql-enriching-events-with-external-data/?utm_campaign=rmoff&utm_source=demo-scene) and standalone articles [here](https://rmoff.net/2019/12/20/analysing-network-behaviour-with-ksqldb-and-mongodb/?utm_campaign=rmoff&utm_source=demo-scene) and [here](https://rmoff.net/2019/12/18/detecting-and-analysing-ssh-attacks-with-ksqldb/?utm_campaign=rmoff&utm_source=demo-scene))
    - [Azure SQL Data Warehouse Connector Sink Demo](azure-sqldw-sink-connector)
    - [IBM MQ Connect Connector Demo](cp-all-in-one-ibmmq)
    - [Solace Sink/Source Demo](solace)

###  Confluent Cloud

- [Getting Started with Confluent Cloud using Java](getting-started-with-ccloud-java)
- [Getting Started with Confluent Cloud using Go](getting-started-with-ccloud-golang)
- [Streaming Pac-Man](streaming-pacman)
- ["The Cube" Demo](ccloud-cube-demo)
- [Using Replicator with Confluent Cloud](ccloud-replicator)
- [Streaming ETL pipeline from MongoDB to Snowflake with Apache Kafka®](streaming-etl-mongodb-snowflake)
- [Micronaut & AWS Lambda on Confluent Cloud](micronaut-lambda)
- [Bridge to Cloud (and back!) with Confluent and MongoDB Atlas](mongodb-demo)

###  Confluent Platform

- [Self-Balancing Clusters Demo](self-balancing)
- [Tiered Storage Demo](tiered-storage)
- [Cluster Linking Demo](cluster-linking)
- [Confluent Admin REST APIs Demo](adminrest)
- [CP-Ansible on Ansible Tower](ansible-tower)

###  Misc
- [Kafka as a Platform: the Ecosystem from the Ground Up](kafka-ecosystem) ([🎥 recording](https://www.youtube.com/watch?v=qjTZ4UeJdoI))
- [Hacky export/import between Kafka clusters](export-import-with-kafkacat) using `kafkacat`
- Docker Compose for just the [community licensed components of Confluent Platform](community-components-only)
- [Topic Tailer](topic-tailer), stream topics to the browser using websockets
- [KPay payment processing example](scalable-payment-processing)
- [Industry themes (e.g. banking Next Best Offer)](industry-themes)
- [Distributed tracing](distributed-tracing)
- [Analysing Sonos data in Kafka](sonos) ([✍️ blog](https://rmoff.net/2020/01/21/monitoring-sonos-with-ksqldb-influxdb-and-grafana/))
- [Analysing Wi-Fi pcap data with Kafka](wifi-fun)
- [Twitter streams](twitter-streams) and [Operator](twitter-streams-operator)

## Feedback & Questions

* Bugs/issues with demo: raise an issue on this github project
* General question/assistance: https://confluent.io/community/ask-the-community/
