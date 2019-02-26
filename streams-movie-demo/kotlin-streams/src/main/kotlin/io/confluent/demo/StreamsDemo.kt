package io.confluent.demo


import io.confluent.demo.util.CountAndSum
import io.confluent.demo.util.CountAndSumDeserializer
import io.confluent.demo.util.CountAndSumSerializer
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerConfig.MAX_BLOCK_MS_CONFIG
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serdes.*
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig.*
import org.apache.kafka.streams.kotlin.consumedWith
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueStore
import java.util.*
import java.util.Collections.singletonMap
import kotlin.concurrent.thread

private const val SCHEMA_REGISTRY_URL = "http://localhost:8081"
private const val KAFKA_BOOTSTRAP_SERVER = "localhost:9092"

private const val RAW_RATINGS_TOPIC_NAME = "raw-ratings"
private const val AVERAGE_RATINGS_TOPIC_NAME = "average-ratings"
private const val RAW_MOVIES_TOPIC_NAME = "raw-movies"
private const val RATED_MOVIES_TOPIC_NAME = "rated-movies"

//TODO
fun main(args: Array<String>) {

  var configPath = ""
  if (args.isNotEmpty()) configPath = args[0]
  val config = getStreamsConfig(KAFKA_BOOTSTRAP_SERVER, SCHEMA_REGISTRY_URL, configPath)

  val serdeConfig = getSerdeConfig(SCHEMA_REGISTRY_URL)

  val movieSerde = getMovieAvroSerde(serdeConfig)
  val ratingSerde = getRatingAvroSerde(serdeConfig)
  // TODO: use it as final result
  val ratedMovieSerde = getRatedMovieAvroSerde(serdeConfig)

  // Starting creating topology
  val builder = StreamsBuilder()

  // Ratings processor
  val rawRatingsStream = getRawRatingsStream(builder)
  val ratingAverage = getRatingAverageTable(rawRatingsStream)

  // Movies processors
  val movies = getMoviesTable(builder, movieSerde)

  getRatedMoviesTable(movies, ratingAverage, ratedMovieSerde)

  // finish the topology
  val topology = builder.build()
  println(topology.describe().toString())
  val streamsApp = KafkaStreams(topology, config)

  Runtime.getRuntime().addShutdownHook(thread(start = false) { streamsApp.close() })
  streamsApp.start()
}

private fun getMoviesTable(builder: StreamsBuilder,
                           movieSerde: SpecificAvroSerde<Movie>): KTable<Long, Movie> {
  val rawMovies = getRawMoviesStream(builder)

  // Parsed movies
  rawMovies
          .mapValues(ValueMapper<String, Movie> { Parser.parseMovie(it) })
          .map { _, movie -> KeyValue(movie.movieId, movie) }
          .to("movies", Produced.with(Long(), movieSerde))

  // Movies table
  // TODO: replace with extension function
  return builder.table("movies",
          Materialized.`as`<Long, Movie, KeyValueStore<Bytes, ByteArray>>("movies-store")
                  .withValueSerde(movieSerde)
                  .withKeySerde(Long()))
}

private fun getRawMoviesStream(builder: StreamsBuilder): KStream<Long, String> {
  return builder.stream(RAW_MOVIES_TOPIC_NAME, consumedWith<Long, String>())

}

private fun getRawRatingsStream(builder: StreamsBuilder): KStream<Long, String> {
  return builder.stream(RAW_RATINGS_TOPIC_NAME, consumedWith<Long, String>())
}

private fun getSerdeConfig(srUrl: String): Map<String, String> {
  return singletonMap<String, String>(SCHEMA_REGISTRY_URL_CONFIG, srUrl)
}

private fun getRatingAvroSerde(serdeConfig: Map<String, String>): SpecificAvroSerde<Rating> {
  val ratingSerde = SpecificAvroSerde<Rating>()
  ratingSerde.configure(serdeConfig, false)
  return ratingSerde
}

private fun getMovieAvroSerde(serdeConfig: Map<String, String>): SpecificAvroSerde<Movie> {
  val movieSerde = SpecificAvroSerde<Movie>()
  movieSerde.configure(serdeConfig, false)
  return movieSerde
}

private fun getRatedMovieAvroSerde(serdeConfig: Map<String, String>): SpecificAvroSerde<RatedMovie> {
  val ratedMovieSerde = SpecificAvroSerde<RatedMovie>()
  ratedMovieSerde.configure(serdeConfig, false)
  return ratedMovieSerde
}

private fun getRatedMoviesTable(movies: KTable<Long, Movie>,
                                ratingAverage: KTable<Long, Double>,
                                ratedMovieSerde: SpecificAvroSerde<RatedMovie>): KTable<Long, RatedMovie> {

  val joiner = { avg: Double, movie: Movie ->
    RatedMovie(movie.movieId,
            movie.getTitle(),
            movie.releaseYear,
            avg)
  }
  val ratedMovies = ratingAverage.join(movies, joiner)

  ratedMovies.toStream().to(RATED_MOVIES_TOPIC_NAME, Produced.with(Long(), ratedMovieSerde))
  return ratedMovies
}

private fun getRatingAverageTable(rawRatings: KStream<Long, String>): KTable<Long, Double> {

  val ratings = rawRatings
          .mapValues(ValueMapper<String, Rating> { Parser.parseRating(it) })
          .map { _, rating -> KeyValue(rating.movieId, rating) }

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
          { _, value, aggregate ->
            ++aggregate.count
            aggregate.sum += value
            aggregate
          }, Materialized.with(Serdes.LongSerde(),
          serdeFrom<CountAndSum<Long, Double>>(CountAndSumSerializer<Long, Double>(), CountAndSumDeserializer<Long, Double>()))
  )

  /*KTable<Long, Double> ratingAverage = ratingSums.join(ratingCounts,
                                                       (sum, count) -> sum / count.doubleValue(),
                                                       Materialized.as("average-ratings"));*/
  val ratingAverage = ratingCountAndSum.mapValues({ value -> value.sum / value.count },
          Materialized.`as`("average-ratings"))
  ratingAverage.toStream()
          .peek { // debug only
            key, value ->
            println("key = $key, value = $value")
          }
          .to(AVERAGE_RATINGS_TOPIC_NAME)
  return ratingAverage
}

private fun getStreamsConfig(kafkaBootStrapServer: String,
                             schemaRegistryUrl: String, configPath: String?): Properties {
  val config: Properties
  if (configPath != null && !configPath.isEmpty()) {
    config = ConfigLoader.loadConfig(configPath)
  } else {
    config = Properties()
    config[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaBootStrapServer
  }

  /*config.put(PRODUCER_PREFIX + ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
             "io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor");

  config.put(CONSUMER_PREFIX + ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG,
             "io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor");*/

  config.putAll(
          mapOf(
                  // TODO: make configurable
                  REPLICATION_FACTOR_CONFIG to 1,
                  APPLICATION_ID_CONFIG to "kafka-films",
                  SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl,
                  DEFAULT_KEY_SERDE_CLASS_CONFIG to Long().javaClass.name,
                  DEFAULT_VALUE_SERDE_CLASS_CONFIG to Double().javaClass.name,
                  // start from the beginning 
                  // TODO: make this configurable 
                  ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",

                  // config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
                  // Enable record cache of size 10 MB.
                  CACHE_MAX_BYTES_BUFFERING_CONFIG to (10 * 1024 * 1024L),
                  // Set commit interval to 1 second.
                  COMMIT_INTERVAL_MS_CONFIG to 1000,
                  topicPrefix("segment.ms") to 15000000,

                  // from https://docs.confluent.io/current/cloud/connect/streams-cloud-config.html
                  // Recommended performance/resilience settings
                  producerPrefix(ProducerConfig.RETRIES_CONFIG) to 2147483647,
                  // ccloud workaround 
                  "producer.confluent.batch.expiry.ms" to 9223372036854775807L,
                  producerPrefix(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG) to 300000,
                  producerPrefix(MAX_BLOCK_MS_CONFIG) to 9223372036854775807L
          ))
  return config
}
