# OSQuery and Confluent Platform

This is an example of how to use [OSQuery's builtin Kafka Logger](https://osquery.readthedocs.io/en/stable/deployment/logging/#logging-as-a-kafka-producer) to send OSQuery results to Confluent Platform.

## OSQuery Kafka Logger Configuration Overview

OSQuery has 3 command line tools:

* osqueryd - the daemon that runs queries in the background
* osqueryi - a cli to execute queries and display them in std out
* osqueryctl - that controls the osqueryd process

The configration below is passed to osqueryd. Create a file called `config.json` and past the config below.

```json
{
    "options": {
        "logger_kafka_brokers": "broker:29092",
        "logger_kafka_compression": "lz4",
        "logger_kafka_acks": "all"
    },
    "packs": {
      "system": {
        "queries": {
          "system_info": {
            "query": "select * from system_info",
            "interval": 10
          },
          "processes": {
            "query": "select * from processes",
            "interval": 10
          }
        }
      }
    },
    "kafka_topics": {
      "system_info": [
        "pack_system_system_info"
      ],
      "processes": [
        "pack_system_processes"
      ]
    }
  }
```

* `options` - holds the kafka configuration parameters. TLS is also supported. See the Kafka producer documentation in OSQuery for instructions.

### Executing osqueryd

```bash
osqueryd --config_path=/project/osquery/config.json --logger_plugin=kafka_producer
```

* `--config_path` is the path to the configuration above.
* `--logger_plugin` instructs osquery to use the Kafka Producer when serializing the logs.
