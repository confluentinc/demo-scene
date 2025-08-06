# Word Count with Kafka Streams Interactive Queries

This project demonstrates how to use Kafka Streams Interactive Queries to build a distributed, stateful stream processing application with queryable state stores. The example implements a real-time word counting application that exposes its state through a REST API.

## Overview

The application processes text messages from a Kafka topic, counts word occurrences, and stores the results in two state stores:

- **word-count**: A key-value store containing all-time word counts
- **windowed-word-count**: A windowed store containing per-minute word counts

The state stores are distributed across multiple application instances, and the Interactive Queries API allows you to query any instance to retrieve data from any store, regardless of which instance actually hosts the data.

## Architecture

### Core Components

- **WordCountInteractiveQueriesExample**: Main application that defines the stream processing topology
- **WordCountInteractiveQueriesRestService**: Jetty-based REST API that exposes state store data via HTTP endpoints
- **WordCountInteractiveQueriesDriver**: Sample data generator for testing
- **MetadataService**: Handles discovery of which application instance contains specific data
- **HostStoreInfo**: Represents metadata about application instances and their state stores
- **KeyValueBean**: JSON-serializable response format for API endpoints

### Stream Processing Topology

1. **Input**: Reads text lines from `TextLinesTopic`
2. **Processing**:
   - Splits text into words (lowercase, non-word characters as delimiters)
   - Groups by word
   - Counts occurrences
3. **Output**: Maintains two state stores:
   - All-time word counts (KeyValue store)
   - 1-minute windowed word counts (Window store)

## Run the application

### Prerequisites

* Java 17 or later
* Maven 3.6 or later
* Docker running via [Docker Desktop](https://docs.docker.com/desktop/) or [Docker Engine](https://docs.docker.com/engine/install/)
* The [Confluent CLI](https://docs.confluent.io/confluent-cli/current/install.html) installed on your machine

### 1. Start Kafka

```bash
confluent local kafka start
```

Note the `Plaintext Ports` port, which is the port that you will specify as part of the bootstrap servers endpoint in later steps.

### 2. Create the input topic

```bash
confluent local kafka topic create TextLinesTopic --partitions 3 --replication-factor 1
```

### 3. Build the application

```bash
mvn clean package -DskipTests
```

### 4. Start Kafka Streams application instances

Start multiple instances to see the distributed nature of the application. Pass the `Plaintext Ports` output when you started Kafka in place of the `<PLAINTEXT_PORT>` placeholder:

**Instance 1 (Port 7070):**

```bash
java -cp target/kafka-streams-examples-8.0.0-standalone.jar \
  io.confluent.examples.streams.interactivequeries.WordCountInteractiveQueriesExample 7070 localhost:<PLAINTEXT_PORT>
```

**Instance 2 (Port 7071):**

```bash
java -cp target/kafka-streams-examples-8.0.0-standalone.jar \
  io.confluent.examples.streams.interactivequeries.WordCountInteractiveQueriesExample 7071 localhost:<PLAINTEXT_PORT>
```

### 5. Generate sample data

```bash
java -cp target/kafka-streams-examples-8.0.0-standalone.jar \
  io.confluent.examples.streams.interactivequeries.WordCountInteractiveQueriesDriver localhost:<PLAINTEXT_PORT>
```

Or manually produce data:

```bash
confluent local kafka produce TextLinesTopic

# Type some text lines:
# hello world
# kafka streams
# hello kafka
```

## Query the IQ-based REST APIs

The application exposes the following endpoints on each instance:

### Instance Discovery

| Endpoint                                | Description                             |
| --------------------------------------- | --------------------------------------- |
| `GET /state/instances`                  | List all running instances              |
| `GET /state/instances/{storeName}`      | List instances hosting a specific store |
| `GET /state/instance/{storeName}/{key}` | Find instance containing a specific key |

### Key-Value Store Queries

| Endpoint                                             | Description                  |
| ---------------------------------------------------- | ---------------------------- |
| `GET /state/keyvalue/{storeName}/{key}`              | Get value for a specific key |
| `GET /state/keyvalues/{storeName}/all`               | Get all key-value pairs      |
| `GET /state/keyvalues/{storeName}/range/{from}/{to}` | Get key-value pairs in range |

### Windowed Store Queries

| Endpoint                                            | Description                               |
| --------------------------------------------------- | ----------------------------------------- |
| `GET /state/windowed/{storeName}/{key}/{from}/{to}` | Get windowed values for key in time range |

### Example API Calls

```bash
# List all instances
curl http://localhost:7070/state/instances

# List instances hosting the word-count store
curl http://localhost:7070/state/instances/word-count

# Get count for word "hello". The endpoint will act as a proxy to the instance hosting this key
# if it's not local.
curl http://localhost:7070/state/keyvalue/word-count/hello

# Get all word counts on this instance.
curl http://localhost:7070/state/keyvalues/word-count/all

# Get word counts in range
curl http://localhost:7070/state/keyvalues/word-count/range/a/m

# Find which instance has the "hello" key
curl http://localhost:7070/state/instance/word-count/hello

# Get windowed counts for "hello" (timestamps in milliseconds) on this instance (does not proxy)
curl "http://localhost:7070/state/windowed/windowed-word-count/hello/1754060800000/1754060956000"
```

## Clean up

When you are finished, stop the Kafka Streams application instances by entering `Ctrl+C` in the terminals where they are running. Then stop Kafka:

```bash
confluent local kafka stop
```

## Key Features

### Distributed State Management

- State stores are automatically partitioned across application instances
- Each instance only stores a subset of the data based on key partitioning
- Cross-instance queries are automatically routed to the correct instance

### Fault Tolerance

- State stores are backed by Kafka topics for durability
- Application instances can be restarted and will rebuild their local state
- Multiple instances provide redundancy

### Real-time Queries

- Query state stores in real-time while stream processing continues
- No need to wait for batch processing or external database updates
- Sub-millisecond query latency for local data

### Interactive Queries

- Discover which instance contains specific data
- Route queries to the appropriate instance automatically
- Unified view across all instances through any endpoint

## Resources

- [Kafka Streams Documentation](https://kafka.apache.org/documentation/streams/)
- [Interactive Queries](https://kafka.apache.org/documentation/streams/developer-guide/interactive-queries.html)
- [Confluent Documentation](https://docs.confluent.io/platform/current/streams/index.html)
