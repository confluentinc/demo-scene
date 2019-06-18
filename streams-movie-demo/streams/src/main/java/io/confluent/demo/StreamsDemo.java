package io.confluent.demo;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.confluent.devx.kafka.config.ConfigLoader;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import static io.confluent.devx.kafka.streams.TopologyVisualizer.visualize;
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.util.Optional.ofNullable;
import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.COMMIT_INTERVAL_MS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.REPLICATION_FACTOR_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.producerPrefix;
import static org.apache.kafka.streams.StreamsConfig.topicPrefix;

public class StreamsDemo {

  private static final String SCHEMA_REGISTRY_URL = "http://localhost:8081";
  private static final String KAFKA_BOOTSTRAP_SERVER = "localhost:9092";

  public static final String RAW_RATINGS_TOPIC_NAME = "raw-ratings";
  public static final String AVERAGE_RATINGS_TOPIC_NAME = "average-ratings";
  public static final String RAW_MOVIES_TOPIC_NAME = "raw-movies";
  public static final String RATED_MOVIES_TOPIC_NAME = "rated-movies";

  public static void main(String args[]) {

    Properties config = getStreamsConfig(KAFKA_BOOTSTRAP_SERVER, SCHEMA_REGISTRY_URL, args[0]);

    final Map<String, String> serdeConfig = getSerdeConfig(config);

    final SpecificAvroSerde<Movie> movieSerde = getMovieAvroSerde(serdeConfig);
    final SpecificAvroSerde<Rating> ratingSerde = getRatingAvroSerde(serdeConfig);

    final SpecificAvroSerde<RatedMovie> ratedMovieSerde = getRatedMovieAvroSerde(serdeConfig);
    final SpecificAvroSerde<CountAndSum> countAndSumSerde = getCountAndSumSerde(serdeConfig);

    // Starting creating topology
    StreamsBuilder builder = new StreamsBuilder();

    // Ratings processor
    KStream<Long, String> rawRatingsStream = getRawRatingsStream(builder);
    KTable<Long, Double> ratingAverage = getRatingAverageTable(rawRatingsStream, countAndSumSerde);

    // Movies processors
    final KTable<Long, Movie> movies = getMoviesTable(builder, movieSerde);

    getRatedMoviesTable(movies, ratingAverage, ratedMovieSerde);

    // finish the topology
    Topology topology = builder.build();
    final String topologyString = topology.describe().toString();
    System.out.println(topologyString);
    System.out.println(visualize(topologyString));
    KafkaStreams streamsApp = new KafkaStreams(topology, config);

    Runtime.getRuntime().addShutdownHook(new Thread(streamsApp::close));
    streamsApp.start();
  }

  public static SpecificAvroSerde<CountAndSum> getCountAndSumSerde(Map<String, String> serdeConfig) {
    final SpecificAvroSerde<CountAndSum> avroSerde = new SpecificAvroSerde<>();
    avroSerde.configure(serdeConfig, false);
    return avroSerde;
  }

  public static KTable<Long, Movie> getMoviesTable(StreamsBuilder builder,
                                                   SpecificAvroSerde<Movie> movieSerde) {
    final KStream<Long, String> rawMovies = getRawMoviesStream(builder);

    // Parsed movies
    rawMovies
        .mapValues(Parser::parseMovie)
        .map((key, movie) -> new KeyValue<>(movie.getMovieId(), movie))
        .to("movies", Produced.with(Serdes.Long(), movieSerde));

    // Movies table
    return builder.table("movies",
                         Materialized.<Long, Movie, KeyValueStore<Bytes, byte[]>>as("movies-store")
                             .withValueSerde(movieSerde)
                             .withKeySerde(Serdes.Long()));
  }

  private static KStream<Long, String> getRawMoviesStream(StreamsBuilder builder) {
    return builder.stream(RAW_MOVIES_TOPIC_NAME,
                          Consumed.with(Serdes.Long(),
                                        Serdes.String()));
  }

  protected static KStream<Long, String> getRawRatingsStream(StreamsBuilder builder) {
    return builder.stream(RAW_RATINGS_TOPIC_NAME,
                          Consumed.with(Serdes.Long(),
                                        Serdes.String()));
  }

  protected static Map<String, String> getSerdeConfig(Properties config) {
    final String srUserInfoPropertyName = "schema.registry.basic.auth.user.info";
    final HashMap<String, String> map = new HashMap<>();

    final String srUrlConfig = config.getProperty(SCHEMA_REGISTRY_URL_CONFIG);
    final String srAuthCredsConfig = config.getProperty(BASIC_AUTH_CREDENTIALS_SOURCE);
    final String srUserInfoConfig = config.getProperty(srUserInfoPropertyName);

    map.put(SCHEMA_REGISTRY_URL_CONFIG, ofNullable(srUrlConfig).orElse(""));
    map.put(BASIC_AUTH_CREDENTIALS_SOURCE, ofNullable(srAuthCredsConfig).orElse(""));
    map.put(srUserInfoPropertyName, ofNullable(srUserInfoConfig).orElse(""));
    return map;
  }

  private static SpecificAvroSerde<Rating> getRatingAvroSerde(Map<String, String> serdeConfig) {
    final SpecificAvroSerde<Rating> ratingSerde = new SpecificAvroSerde<>();
    ratingSerde.configure(serdeConfig, false);
    return ratingSerde;
  }

  public static SpecificAvroSerde<Movie> getMovieAvroSerde(Map<String, String> serdeConfig) {
    final SpecificAvroSerde<Movie> movieSerde = new SpecificAvroSerde<>();
    movieSerde.configure(serdeConfig, false);
    return movieSerde;
  }

  public static SpecificAvroSerde<RatedMovie> getRatedMovieAvroSerde(Map<String, String> serdeConfig) {
    SpecificAvroSerde<RatedMovie> ratedMovieSerde = new SpecificAvroSerde<>();
    ratedMovieSerde.configure(serdeConfig, false);
    return ratedMovieSerde;
  }

  public static KTable<Long, RatedMovie> getRatedMoviesTable(KTable<Long, Movie> movies,
                                                             KTable<Long, Double> ratingAverage,
                                                             SpecificAvroSerde<RatedMovie> ratedMovieSerde) {

    ValueJoiner<Double, Movie, RatedMovie> joiner = (avg, movie) -> new RatedMovie(movie.getMovieId(),
                                                                                   movie.getTitle(),
                                                                                   movie.getReleaseYear(),
                                                                                   avg);
    KTable<Long, RatedMovie>
        ratedMovies =
        ratingAverage
            .join(movies, joiner,
                  Materialized.<Long, RatedMovie, KeyValueStore<Bytes, byte[]>>as("rated-movies-store")
                      .withValueSerde(ratedMovieSerde)
                      .withKeySerde(Serdes.Long()));

    ratedMovies.toStream().to(RATED_MOVIES_TOPIC_NAME, Produced.with(Serdes.Long(), ratedMovieSerde));
    return ratedMovies;
  }

  protected static KTable<Long, Double> getRatingAverageTable(KStream<Long, String> rawRatings,
                                                              SpecificAvroSerde<CountAndSum> countAndSumSerde) {

    KStream<Long, Rating> ratings = rawRatings.mapValues(Parser::parseRating)
        .map((key, rating) -> new KeyValue<>(rating.getMovieId(), rating));

    // Parsing Ratings

    KGroupedStream<Long, Double> ratingsById = ratings.mapValues(Rating::getRating).groupByKey();

    /*
    As per Matthias' comment https://issues.apache.org/jira/browse/KAFKA-7595?focusedCommentId=16677173&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-16677173
    implementation

    KTable<Long, Long> ratingCounts = ratingsById.count();
    KTable<Long, Double> ratingSums = ratingsById.reduce((v1, v2) -> v1 + v2);

    is incorrect

    Implemented best practice https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Stream+Usage+Patterns#KafkaStreamUsagePatterns-Howtocomputean(windowed)average?
     */
    final KTable<Long, CountAndSum> ratingCountAndSum = ratingsById.aggregate(
        () -> new CountAndSum(0L, 0.0),
        (key, value, aggregate) -> {
          aggregate.setCount(aggregate.getCount() + 1);
          aggregate.setSum(aggregate.getSum() + value);
          return aggregate;
        }, Materialized.with(Serdes.Long(), countAndSumSerde)
    );

    final KTable<Long, Double>
        ratingAverage =
        ratingCountAndSum.mapValues(value -> value.getSum() / value.getCount(),
                                    Materialized.as("average-ratings"));

    ratingAverage.toStream()
        .peek((key, value) -> { // debug only
          System.out.println("key = " + key + ", value = " + value);
        })
        .to(AVERAGE_RATINGS_TOPIC_NAME);
    return ratingAverage;
  }

  public static Properties getStreamsConfig(String kafkaBootStrapServer,
                                            String schemaRegistryUrl, String configPath) {
    final Properties config;
    if (configPath != null && !configPath.isEmpty()) {
      config = ConfigLoader.loadConfig(configPath);
    } else {
      // assuming that we're running locally or url's explicitly provided
      config = new Properties();
      config.put(BOOTSTRAP_SERVERS_CONFIG, kafkaBootStrapServer);
      config.put(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
    }

    config.put(REPLICATION_FACTOR_CONFIG, 1);

    /*config.put(PRODUCER_PREFIX + ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
               "io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor");

    config.put(CONSUMER_PREFIX + ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG,
               "io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor");*/
    config.put(APPLICATION_ID_CONFIG, "kafka-films");
    config.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());
    config.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Double().getClass().getName());

    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    // config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
    // Enable record cache of size 2 MB.
    config.put(CACHE_MAX_BYTES_BUFFERING_CONFIG, 2 * 1024 * 1024L);
    // Set commit interval to 1 second.
    config.put(COMMIT_INTERVAL_MS_CONFIG, 1000);
    config.put(topicPrefix("segment.ms"), 15000000);

    // from https://docs.confluent.io/current/cloud/connect/streams-cloud-config.html
    // Recommended performance/resilience settings
    config.put(producerPrefix(ProducerConfig.RETRIES_CONFIG), 2147483647);
    config.put("producer.confluent.batch.expiry.ms", 9223372036854775807L);
    config.put(producerPrefix(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG), 300000);
    config.put(producerPrefix(ProducerConfig.MAX_BLOCK_MS_CONFIG), 9223372036854775807L);
    return config;
  }

}