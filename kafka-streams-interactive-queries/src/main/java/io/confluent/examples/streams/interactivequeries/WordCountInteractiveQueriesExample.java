/*
 * Copyright Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.confluent.examples.streams.interactivequeries;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;


/**
 * Demonstrates using the KafkaStreams API to locate and query State Stores (Interactive Queries).
 *
 * <p>Note: This example uses Java 8 functionality and thus works with Java 8+ only.  But of course you
 * can use the Interactive Queries feature of Kafka Streams also with Java 7.
 *
 * <p>In this example, the input stream reads from a topic named "TextLinesTopic", where the values of
 * messages represent lines of text; and the histogram output is exposed via two State Stores:
 * word-count (KeyValue) and windowed-word-count (Windowed Store).
 *
 * <p>The word-count store contains the all time word-count. The windowed-word-count contains per
 * minute word-counts.
 *
 * <p>Note: Before running this example you must 1) create the source topic (e.g. via `kafka-topics
 * --create ...`), then 2) start this example and 3) write some data to the source topic (e.g. via
 * `kafka-console-producer`). Otherwise you won't see any data arriving in the output topic.
 *
 * <p>HOW TO RUN THIS EXAMPLE
 *
 * <p>1) Start Zookeeper and Kafka. Please refer to <a href="http://docs.confluent.io/current/quickstart.html#quickstart">QuickStart</a>.
 *
 * <p>2) Create the input and output topics used by this example.
 *
 * <pre>
 * {@code
 * $ bin/kafka-topics --create --topic TextLinesTopic \
 *                    --zookeeper localhost:2181 --partitions 3 --replication-factor 1
 * }
 * </pre>
 *
 * Note: The above commands are for the Confluent Platform. For Apache Kafka it should be
 * `bin/kafka-topics.sh ...`.
 *
 * <p>3) Start two instances of this example application either in your IDE or on the command line.
 *
 * <p>If via the command line please refer to <a href="https://github.com/confluentinc/kafka-streams-examples#packaging-and-running">Packaging</a>.
 *
 * <p>Once packaged you can then start the first instance of the application (on port 7070):
 *
 * <pre>
 * {@code
 * $ java -cp target/kafka-streams-examples-8.0.0-standalone.jar \
 *      io.confluent.examples.streams.interactivequeries.WordCountInteractiveQueriesExample 7070
 * }
 * </pre>
 *
 * Here, `7070` sets the port for the REST endpoint that will be used by this application instance.
 *
 * <p>Then, in a separate terminal, run the second instance of this application (on port 7071):
 *
 * <pre>
 * {@code
 * $ java -cp target/kafka-streams-examples-8.0.0-standalone.jar \
 *      io.confluent.examples.streams.interactivequeries.WordCountInteractiveQueriesExample 7071
 * }
 * </pre>
 *
 *
 * 4) Write some input data to the source topics (e.g. via {@link WordCountInteractiveQueriesDriver}). The
 * already running example application (step 3) will automatically process this input data
 *
 * <p>5) Use your browser to hit the REST endpoint of the app instance you started in step 3 to query
 * the state managed by this application.  Note: If you are running multiple app instances, you can
 * query them arbitrarily -- if an app instance cannot satisfy a query itself, it will fetch the
 * results from the other instances.
 *
 * <p>For example:
 *
 * <pre>
 * {@code
 * # List all running instances of this application
 * http://localhost:7070/state/instances
 *
 * # List app instances that currently manage (parts of) state store "word-count"
 * http://localhost:7070/state/instances/word-count
 *
 * # Get all key-value records from the "word-count" state store hosted on a the instance running
 * # localhost:7070
 * http://localhost:7070/state/keyvalues/word-count/all
 *
 * # Find the app instance that contains key "hello" (if it exists) for the state store "word-count"
 * http://localhost:7070/state/instance/word-count/hello
 *
 * # Get the latest value for key "hello" in state store "word-count"
 * http://localhost:7070/state/keyvalue/word-count/hello
 * }
 * </pre>
 *
 * Note: that the REST functionality is NOT part of Kafka Streams or its API. For demonstration
 * purposes of this example application, we decided to go with a simple, custom-built REST API that
 * uses the Interactive Queries API of Kafka Streams behind the scenes to expose the state stores of
 * this application via REST.
 *
 * <p>6) Once you're done with your experiments, you can stop this example via `Ctrl-C`.  If needed,
 * also stop the Kafka broker (`Ctrl-C`), and only then stop the ZooKeeper instance (`Ctrl-C`).
 *
 * <p>If you like you can run multiple instances of this example by passing in a different port. You
 * can then experiment with seeing how keys map to different instances etc.
 */
public class WordCountInteractiveQueriesExample {

  static final String TEXT_LINES_TOPIC = "TextLinesTopic";
  static final String DEFAULT_HOST = "localhost";

  public static void main(final String[] args) throws Exception {
    if (args.length == 0 || args.length > 2) {
      throw new IllegalArgumentException("usage: ... <portForRestEndPoint> [<bootstrap.servers> (optional)]");
    }
    final int port = Integer.parseInt(args[0]);
    final String bootstrapServers = args.length > 1 ? args[1] : "localhost:9092";

    final Properties streamsConfiguration = new Properties();
    // Give the Streams application a unique name.  The name must be unique in the Kafka cluster
    // against which the application is run.
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "interactive-queries-example");
    streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "interactive-queries-example-client");
    // Where to find Kafka broker(s).
    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    // Set the default key serde
    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
    // Set the default value serde
    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
    // Provide the details of our embedded http service that we'll use to connect to this streams
    // instance and discover locations of stores.
    streamsConfiguration.put(StreamsConfig.APPLICATION_SERVER_CONFIG, DEFAULT_HOST + ":" + port);
    final File example = Files.createTempDirectory(new File("/tmp").toPath(), "example").toFile();
    streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, example.getPath());

    final KafkaStreams streams = createStreams(streamsConfiguration);
    // Always (and unconditionally) clean local state prior to starting the processing topology.
    // We opt for this unconditional call here because this will make it easier for you to play around with the example
    // when resetting the application for doing a re-run (via the Application Reset Tool,
    // https://docs.confluent.io/platform/current/streams/developer-guide/app-reset-tool.html).
    //
    // The drawback of cleaning up local state prior is that your app must rebuilt its local state from scratch, which
    // will take time and will require reading all the state-relevant data from the Kafka cluster over the network.
    // Thus in a production scenario you typically do not want to clean up always as we do here but rather only when it
    // is truly needed, i.e., only under certain conditions (e.g., the presence of a command line flag for your app).
    // See `ApplicationResetExample.java` for a production-like example.
    streams.cleanUp();
    // Now that we have finished the definition of the processing topology we can actually run
    // it via `start()`.  The Streams application as a whole can be launched just like any
    // normal Java application that has a `main()` method.
    streams.start();

    // Start the Restful proxy for servicing remote access to state stores
    final WordCountInteractiveQueriesRestService restService = startRestProxy(streams, DEFAULT_HOST, port);

    // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        streams.close();
        restService.stop();
      } catch (final Exception e) {
        // ignored
      }
    }));
  }


  static WordCountInteractiveQueriesRestService startRestProxy(final KafkaStreams streams,
                                                               final String host,
                                                               final int port) throws Exception {
    final HostInfo hostInfo = new HostInfo(host, port);
    final WordCountInteractiveQueriesRestService
        wordCountInteractiveQueriesRestService = new WordCountInteractiveQueriesRestService(streams, hostInfo);
    wordCountInteractiveQueriesRestService.start(port);
    return wordCountInteractiveQueriesRestService;
  }

  static KafkaStreams createStreams(final Properties streamsConfiguration) {
    final Serde<String> stringSerde = Serdes.String();
    final StreamsBuilder builder = new StreamsBuilder();
    final KStream<String, String>
        textLines = builder.stream(TEXT_LINES_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));

    final KGroupedStream<String, String> groupedByWord = textLines
        .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
        .groupBy((key, word) -> word, Grouped.with(stringSerde, stringSerde));

    // Create a State Store for with the all time word count
    groupedByWord.count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("word-count")
        .withValueSerde(Serdes.Long()));

    // Create a Windowed State Store that contains the word count for every
    // 1 minute
    groupedByWord.windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
        .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as("windowed-word-count")
            .withValueSerde(Serdes.Long()));

    return new KafkaStreams(builder.build(), streamsConfiguration);
  }

}
