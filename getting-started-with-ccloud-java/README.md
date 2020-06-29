# Getting Started with Confluent Cloud
Example of Java application that produces and consumes Protobuf-based data to/from a Kafka cluster in Confluent Cloud

## Running

### 1 - Update the configuration

```bash
cd src/main/resources
vim ccloud.properties
```

### 2 - Building the project

```bash
mvn clean package
```

### 3 - Running the producer

```bash
java -cp target/getting-started-with-ccloud-1.0.jar io.confluent.cloud.demo.ProducerApp
```

### 4 - Running the consumer

```bash
java -cp target/getting-started-with-ccloud-1.0.jar io.confluent.cloud.demo.ConsumerApp
```

# License

This project is licensed under the [Apache 2.0 License](./LICENSE).