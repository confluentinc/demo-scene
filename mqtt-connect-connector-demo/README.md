# Apache Kafka + Kafka Connect + MQTT Connector + Sensor Data

This demo shows an Internet of Things (IoT) integration example using Apache Kafka + Kafka Connect + MQTT Connector + Sensor Data.

This project does not include any source code as Kafka Connect allows integration with data sources and sinks just with configuration. 

Example configuration and step-by-step guide can be found below.

## Kafka Connect Configuration (No Source Code Needed!)
Here is the full configuration for the MQTT Connector for Kafka Connect's Standalone mode, which we use with Confluent CLI for a local setup: 

                name=MqttSourceConnector1
                connector.class=io.confluent.connect.mqtt.MqttSourceConnector
                tasks.max=1
                mqtt.server.uri=< Required Configuration >
                mqtt.topics=< Required Configuration >


For distributed mode, you can use the same configuration with REST API:

                curl -s -X POST -H 'Content-Type: application/json' http://localhost:8083/connectors -d '{
                    "name" : "< Required Configuration >",
                "config" : {
                    "connector.class" : "io.confluent.connect.mqtt.MqttSourceConnector",
                    "tasks.max" : "1",
                    "mqtt.server.uri" : "< Required Configuration >",
                    "mqtt.topics" : "< Required Configuration >",
                    "kafka.topics" : "< Required Configuration >"
                }
                }'


The documentation explains the [differences between standalone and distributed Kafka Connect mode](https://docs.confluent.io/current/connect/concepts.html#connect-concepts). In short: Standalone mode is the simplest mode, where a single process is responsible for executing all connectors and tasks. Distributed mode is used in most production scenarios and provides scalability and automatic fault tolerance for Kafka Connect.






Confluent documentation contains more details about installing and using [Confluent's MQTT Connector](https://docs.confluent.io/current/connect/kafka-connect-mqtt).

## How to run it?

### Requirements
- Java 8
- [Confluent Platform 5.0+](https://www.confluent.io/download/) (Confluent Enterprise if you want to use the Confluent MQTT Proxy, Confluent Open Source if you just want to run the KSQL UDF and send data via kafkacat instead of MQTT)
- MQTT Client and Broker (this demo uses [Mosquitto](https://mosquitto.org/download/))
- [Confluent MQTT Connector](https://www.confluent.io/connector/kafka-connect-mqtt/) (a Kafka Connect based connector to send and receive MQTT messages) - Very easy installation via Confluent Hub, just one command:

                confluent-hub install confluentinc/kafka-connect-mqtt:1.0.0-preview
- Optional: [MQTT.fx](https://mqttfx.jensd.de/) (a nice, simple UI to test MQTT pub/sub; not required - just makes life more comfortable)

### Step-by-step demo
Follow these steps to [configure the MQTT Connector, start all components, generate MQTT sensor data and consume it from a Kafka consumer](https://github.com/kaiwaehner/kafka-connect-iot-mqtt-connector-example/blob/master/live-demo-kafka-connect-iot-mqtt-connector.adoc).

## Alternative MQTT Integration with Confluent MQTT Proxy
As alternative to using Kafka Connect, you can also leverage Confluent MQTT Proxy to integrate IoT data from IoT devices directly withou the need for a MQTT Broker. See [Deep Learning UDF for KSQL for Streaming Anomaly Detection of MQTT IoT Sensor Data](https://github.com/kaiwaehner/ksql-udf-deep-learning-mqtt-iot) for an example and source code. 






