package io.confluent.demo

import io.confluent.devx.kafka.config.ConfigLoader
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig.*
import org.apache.kafka.streams.kotlin.KSerdes.consumedWith
import org.apache.kafka.streams.kotlin.KSerdes.producedWith
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueStore
import java.util.*

private const val SCHEMA_REGISTRY_URL = "http://localhost:8081"
private const val KAFKA_BOOTSTRAP_SERVER = "localhost:9092"

private const val RAW_RATINGS_TOPIC_NAME = "raw-ratings"
private const val AVERAGE_RATINGS_TOPIC_NAME = "average-ratings"
const val RAW_MOVIES_TOPIC_NAME = "raw-movies"
const val RATED_MOVIES_TOPIC_NAME = "rated-movies"


fun main(args: Array<String>) {

  var configPath = ""
  if (args.isNotEmpty()) configPath = args[0]
  val config = getStreamsConfig(KAFKA_BOOTSTRAP_SERVER, SCHEMA_REGISTRY_URL, configPath)

  val serdeConfig = getSerdeConfig(config)

  val movieSerde = getMovieAvroSerde(serdeConfig)
  val ratingSerde = getRatingAvroSerde(serdeConfig)
  // TODO: use it as final result
  val ratedMovieSerde = getRatedMovieAvroSerde(serdeConfig)

  // Starting creating topology
  val builder = StreamsBuilder()

  // Ratings processor
  val rawRatingsStream = getRawRatingsStream(builder)
  val countAnsSumSerde = getCountAndSumSerde(serdeConfig)
  val ratingAverage = getRatingAverageTable(rawRatingsStream, countAnsSumSerde)

  // Movies processors
  val movies = getMoviesTable(builder, movieSerde)

  getRatedMoviesTable(movies, ratingAverage, ratedMovieSerde)

  // finish the topology
  val topology = builder.build()
  println(topology.describe().toString())
  val streamsApp = KafkaStreams(topology, config)

  Runtime.getRuntime().addShutdownHook(Thread(Runnable { streamsApp.close() }))
  streamsApp.start()

}

fun getMoviesTable(builder: StreamsBuilder,
                   movieSerde: SpecificAvroSerde<Movie>): KTable<Long, Movie> {
  val rawMovies = getRawMoviesStream(builder)

  // Parsed movies
  rawMovies
          .mapValues<Movie>(ValueMapper<String, Movie> { Parser.parseMovie(it) })
          .map { _, movie -> KeyValue<Long, Movie>(movie.movieId, movie) }
          .to("movies", Produced.with(Serdes.Long(), movieSerde))

  // Movies table
  return builder.table("movies",
          Materialized.`as`<Long, Movie, KeyValueStore<Bytes, ByteArray>>("movies-store")
                  .withValueSerde(movieSerde)
                  .withKeySerde(Serdes.Long()))
}

internal fun getRawMoviesStream(builder: StreamsBuilder): KStream<Long, String> {
  return builder.stream(RAW_MOVIES_TOPIC_NAME, consumedWith<Long, String>())
}

internal fun getRawRatingsStream(builder: StreamsBuilder): KStream<Long, String> {
  return builder.stream(RAW_RATINGS_TOPIC_NAME, consumedWith<Long, String>())

}

private fun getSerdeConfig(config: Properties): Map<String, String> {
  val srUserInfoPropertyName = "schema.registry.basic.auth.user.info"

  return mapOf(
          SCHEMA_REGISTRY_URL_CONFIG to (config.getProperty(SCHEMA_REGISTRY_URL_CONFIG) ?: ""),
          BASIC_AUTH_CREDENTIALS_SOURCE to (config.getProperty(BASIC_AUTH_CREDENTIALS_SOURCE) ?: ""),
          srUserInfoPropertyName to (config.getProperty(srUserInfoPropertyName) ?: "")
  )
}

private fun getRatingAvroSerde(serdeConfig: Map<String, String>): SpecificAvroSerde<Rating> {
  val ratingSerde = SpecificAvroSerde<Rating>()
  ratingSerde.configure(serdeConfig, false)
  return ratingSerde
}

fun getMovieAvroSerde(serdeConfig: Map<String, String>): SpecificAvroSerde<Movie> {
  val movieSerde = SpecificAvroSerde<Movie>()
  movieSerde.configure(serdeConfig, false)
  return movieSerde
}

fun getRatedMovieAvroSerde(serdeConfig: Map<String, String>): SpecificAvroSerde<RatedMovie> {
  val ratedMovieSerde = SpecificAvroSerde<RatedMovie>()
  ratedMovieSerde.configure(serdeConfig, false)
  return ratedMovieSerde
}

fun getCountAndSumSerde(serdeConfig: Map<String, String>): SpecificAvroSerde<CountAndSum> {
  val avroSerde = SpecificAvroSerde<CountAndSum>()
  avroSerde.configure(serdeConfig, false)
  return avroSerde
}

fun getRatedMoviesTable(movies: KTable<Long, Movie>,
                        ratingAverage: KTable<Long, Double>,
                        ratedMovieSerde: SpecificAvroSerde<RatedMovie>): KTable<Long, RatedMovie> {

  val joiner = { avg: Double, movie: Movie ->
    RatedMovie(movie.movieId,
            movie.getTitle(),
            movie.releaseYear,
            avg)
  }
  val ratedMovies = ratingAverage.join(movies, joiner)

  ratedMovies.toStream().to(RATED_MOVIES_TOPIC_NAME, producedWith<Long, RatedMovie>(ratedMovieSerde))
  return ratedMovies
}

internal fun getRatingAverageTable(rawRatings: KStream<Long, String>, countAndSumSerde: Serde<CountAndSum>): KTable<Long, Double> {

  val ratings = rawRatings.mapValues<Rating>(ValueMapper<String, Rating> { Parser.parseRating(it) })
          .map { _, rating -> KeyValue<Long, Rating>(rating.movieId, rating) }

  // Parsing Ratings

  val ratingsById = ratings.mapValues(ValueMapper<Rating, Double> { it.getRating() }).groupByKey()

  /*
  As per Matthias' comment https://issues.apache.org/jira/browse/KAFKA-7595?focusedCommentId=16677173&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-16677173
  implementation

  KTable<Long, Long> ratingCounts = ratingsById.count();
  KTable<Long, Double> ratingSums = ratingsById.reduce((v1, v2) -> v1 + v2);

  is incorrect

  Implemented best practice https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Stream+Usage+Patterns#KafkaStreamUsagePatterns-Howtocomputean(windowed)average?
   */
  val ratingCountAndSum = ratingsById.aggregate(
          { CountAndSum(0L, 0.0) },
          { key, value, aggregate ->
            aggregate.setCount(aggregate.getCount()!! + 1)
            aggregate.setSum(aggregate.getSum()!! + value!!)
            aggregate
          }, Materialized.with(Serdes.Long(), countAndSumSerde)
  )

  /*KTable<Long, Double> ratingAverage = ratingSums.join(ratingCounts,
                                                       (sum, count) -> sum / count.doubleValue(),
                                                       Materialized.as("average-ratings"));*/
  val ratingAverage = ratingCountAndSum.mapValues({ value -> value.sum / value.count },
          Materialized.`as`<Long, Double, KeyValueStore<Bytes, ByteArray>>("average-ratings"))
  ratingAverage.toStream()
          .peek { // debug only
            key, value ->
            println("key = $key, value = $value")
          }
          .to(AVERAGE_RATINGS_TOPIC_NAME)
  return ratingAverage
}

fun getStreamsConfig(kafkaBootStrapServer: String,
                     schemaRegistryUrl: String, configPath: String?): Properties {

  val config: Properties
  if (configPath != null && configPath.isNotEmpty()) {
    config = ConfigLoader.loadConfig(configPath)
  } else {
    // assuming that we're running locally or url's explicitly provided
    config = Properties()
    config[BOOTSTRAP_SERVERS_CONFIG] = kafkaBootStrapServer
    config[SCHEMA_REGISTRY_URL_CONFIG] = schemaRegistryUrl
  }

  config[REPLICATION_FACTOR_CONFIG] = 3

  /*config.put(PRODUCER_PREFIX + ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
             "io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor");

  config.put(CONSUMER_PREFIX + ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG,
             "io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor");*/
  config[APPLICATION_ID_CONFIG] = "kafka-films"
  config[DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.Long().javaClass.name
  config[DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.Double().javaClass.name
  // start from the beginning
  config[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"

  // config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
  // Enable record cache of size 10 MB.
  config[CACHE_MAX_BYTES_BUFFERING_CONFIG] = 10 * 1024 * 1024L
  // Set commit interval to 1 second.
  config[COMMIT_INTERVAL_MS_CONFIG] = 1000
  config[topicPrefix("segment.ms")] = 15000000

  // from https://docs.confluent.io/current/cloud/connect/streams-cloud-config.html
  // Recommended performance/resilience settings
  config[producerPrefix(ProducerConfig.RETRIES_CONFIG)] = 2147483647
  config["producer.confluent.batch.expiry.ms"] = 9223372036854775807L
  config[producerPrefix(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG)] = 300000
  config[producerPrefix(ProducerConfig.MAX_BLOCK_MS_CONFIG)] = 9223372036854775807L


  return config
}