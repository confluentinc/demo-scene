# OSQuery Demonstration

This repository demonstrats two examples of capturing logs from [OSQuery](https://osquery.readthedocs.io/en/stable/) and publishing them to Confluent Cloud and Confluent Platform.

## OSQuery Python Extension Logger Plugin

[confluent_kafka.ext](confluent_kafka.ext) is the python extension that enables OSQuery to write to Apache Kafka, Confluent Platfrom and Confluent Cloud. The default Kafka producer packaged with OSQuery only supports a limited set of producer configurations so it cannot be used to send logs to Confluent Cloud nor can you provide additional settings for tuning the producer. Also the Kafka producer comes with a few known issues which are documented in their GitHub respository.

* [Confluent Cloud](cloud/README.md)
* [Confluent Platform](cp/README.md)
