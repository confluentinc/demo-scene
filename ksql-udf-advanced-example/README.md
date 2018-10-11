# KSQL UDF Advanced Example

Advanced KSQL UDF with dependencies. 
 
Use Case: Connected Cars - Real Time Streaming Analytics using Deep Learning. For more information about the use case and source code, see [Deep Learning UDF for KSQL for Streaming Anomaly Detection of MQTT IoT Sensor Data](https://github.com/kaiwaehner/ksql-udf-deep-learning-mqtt-iot). 

If you want to build your own UDF in a similar way, check out this blog post for a detailed "how to" and potential issues during development and testing: [How to Build a UDF and/or UDAF in KSQL 5.0](https://www.confluent.io/blog/build-udf-udaf-ksql-5-0).

## How to run it?

### Requirements
- Java 8
- [Confluent Platform 5.0+](https://www.confluent.io/download/) (Confluent Enterprise if you want to use the Confluent MQTT Proxy, Confluent Open Source if you just want to run the KSQL UDF and send data via kafkacat instead of MQTT)
- MQTT Client (I use [Mosquitto](https://mosquitto.org/download/) in the demo as MQTT Client - I don't start the server!)
- [kafkacat](https://github.com/edenhill/kafkacat) (optional - if you do not want to use MQTT Producers, and of course you can also use kafka-console-producer instead, but kafkacat is much more comfortable)

### Step-by-step demo

Follow these steps to [deploy the UDF, create MQTT events and process them via KSQL leveraging the analytic model](live-demo.adoc).

## Source Code
Here is the full source code for the [Anomaly Detection KSQL UDF](src/main/java/com/github/megachucky/kafka/streams/machinelearning/Anomaly.java).

