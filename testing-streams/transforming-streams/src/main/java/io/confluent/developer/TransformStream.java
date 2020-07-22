package io.confluent.developer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import io.confluent.developer.avro.Movie;
import io.confluent.developer.avro.RawMovie;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

public class TransformStream {

  public Properties buildStreamsProperties(Properties envProps) {
    Properties props = new Properties();

    props.put(StreamsConfig.APPLICATION_ID_CONFIG, envProps.getProperty("application.id"));
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getProperty("bootstrap.servers"));
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
    props.put(SCHEMA_REGISTRY_URL_CONFIG, envProps.getProperty("schema.registry.url"));

    return props;
  }

  public Topology buildTopology(Properties envProps) {
    final StreamsBuilder builder = new StreamsBuilder();
    final String inputTopic = envProps.getProperty("input.topic.name");

    KStream<String, RawMovie> rawMovies = builder.stream(inputTopic);
    KStream<Long, Movie> movies = rawMovies.map((key, rawMovie) ->
                                                    new KeyValue<>(rawMovie.getId(),
                                                                   convertRawMovie(rawMovie)));

    movies.to("movies", Produced.with(Serdes.Long(), movieAvroSerde(envProps)));

    return builder.build();
  }

  public static Movie convertRawMovie(RawMovie rawMovie) {
    String[] titleParts = rawMovie.getTitle().split("::");
    String title = titleParts[0];
    int releaseYear = Integer.parseInt(titleParts[1]);
    return new Movie(rawMovie.getId(), title, releaseYear, rawMovie.getGenre());
  }

  private SpecificAvroSerde<Movie> movieAvroSerde(Properties envProps) {
    SpecificAvroSerde<Movie> movieAvroSerde = new SpecificAvroSerde<>();

    final HashMap<String, String> serdeConfig = new HashMap<>();
    serdeConfig.put(SCHEMA_REGISTRY_URL_CONFIG,
                    envProps.getProperty("schema.registry.url"));

    movieAvroSerde.configure(serdeConfig, false);
    return movieAvroSerde;
  }

  public void createTopics(Properties envProps) {
    Map<String, Object> config = new HashMap<>();
    config.put("bootstrap.servers", envProps.getProperty("bootstrap.servers"));
    AdminClient client = AdminClient.create(config);

    List<NewTopic> topics = new ArrayList<>();

    topics.add(new NewTopic(
        envProps.getProperty("input.topic.name"),
        Integer.parseInt(envProps.getProperty("input.topic.partitions")),
        Short.parseShort(envProps.getProperty("input.topic.replication.factor"))));

    topics.add(new NewTopic(
        envProps.getProperty("output.topic.name"),
        Integer.parseInt(envProps.getProperty("output.topic.partitions")),
        Short.parseShort(envProps.getProperty("output.topic.replication.factor"))));

    client.createTopics(topics);
    client.close();
  }

  public Properties loadEnvProperties(String fileName) throws IOException {
    Properties envProps = new Properties();
    FileInputStream input = new FileInputStream(fileName);
    envProps.load(input);
    input.close();

    return envProps;
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      throw new IllegalArgumentException(
          "This program takes one argument: the path to an environment configuration file.");
    }

    TransformStream ts = new TransformStream();
    Properties envProps = ts.loadEnvProperties(args[0]);
    Properties streamProps = ts.buildStreamsProperties(envProps);
    Topology topology = ts.buildTopology(envProps);

    ts.createTopics(envProps);

    final KafkaStreams streams = new KafkaStreams(topology, streamProps);
    final CountDownLatch latch = new CountDownLatch(1);

    // Attach shutdown handler to catch Control-C.
    Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
      @Override
      public void run() {
        streams.close();
        latch.countDown();
      }
    });

    try {
      streams.start();
      latch.await();
    } catch (Throwable e) {
      System.exit(1);
    }
    System.exit(0);
  }
}

