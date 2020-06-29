# Getting Started with Confluent Cloud
Example of Go application that produces and consumes Protobuf-based data to/from a Kafka cluster in Confluent Cloud

## Running

### 1 - Update the configuration

```bash
vim ccloud.properties
```

### 2 - Building the project

```bash
go build -o ClientApp SensorReading.pb.go KafkaUtils.go ClientApp.go
```

### 3 - Running the producer

```bash
./ClientApp producer
```

### 4 - Running the consumer

```bash
./ClientApp consumer
```

# License

This project is licensed under the [Apache 2.0 License](./LICENSE).