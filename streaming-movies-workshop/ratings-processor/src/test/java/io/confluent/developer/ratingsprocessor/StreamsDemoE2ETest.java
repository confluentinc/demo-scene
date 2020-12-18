package io.confluent.developer.ratingsprocessor;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.confluent.developer.CountAndSum;
import io.confluent.developer.Movie;
import io.confluent.developer.RatedMovie;
import io.confluent.developer.Rating;
import io.confluent.developer.movies.util.Parser;
import io.confluent.developer.ratingsprocessor.RatingProcessor;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.slf4j.Slf4j;

import static io.confluent.developer.movies.serdes.MySerdes.getSpecificAvroSerde;
import static io.confluent.developer.ratingsprocessor.fixture.MoviesAndRatingsData.DUMMY_KAFKA_CONFLUENT_CLOUD_9092;
import static io.confluent.developer.ratingsprocessor.fixture.MoviesAndRatingsData.DUMMY_SR_CONFLUENT_CLOUD_8080;
import static io.confluent.developer.ratingsprocessor.fixture.MoviesAndRatingsData.LETHAL_WEAPON_MOVIE;
import static io.confluent.developer.ratingsprocessor.fixture.MoviesAndRatingsData.LETHAL_WEAPON_RATING_9;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

@Slf4j
public class StreamsDemoE2ETest {

  private static final String RATINGS_TOPIC_NAME = "ratings";
  private static final String MOVIES_TOPIC_NAME = "movies";
  private static final String RATED_MOVIES_TOPIC_NAME = "rated-movies";
  public static final String AVG_RATINGS_TOPIC_NAME = "average-ratings";
  private TopologyTestDriver td;

  final Map<String, String> serdeConfig = Map.of(SCHEMA_REGISTRY_URL_CONFIG, DUMMY_SR_CONFLUENT_CLOUD_8080);


  @BeforeEach
  public void setUp() throws IOException {

    final Properties p = new Properties();
    p.put("application.id", "kafka-movies-test");
    p.put("bootstrap.servers", DUMMY_KAFKA_CONFLUENT_CLOUD_9092);
    p.put("schema.registry.url", DUMMY_SR_CONFLUENT_CLOUD_8080);
    p.put("default.topic.replication.factor", "1");
    p.put("offset.reset.policy", "latest");

    // workaround https://stackoverflow.com/a/50933452/27563
    final String tempDirectory = Files.createTempDirectory("kafka-streams").toAbsolutePath().toString();
    p.put(StreamsConfig.STATE_DIR_CONFIG, tempDirectory);

    StreamsBuilder builder = new StreamsBuilder();

    final RatingProcessor streamsApp = new RatingProcessor(serdeConfig);

    SpecificAvroSerde<Rating> ratingSerde = getSpecificAvroSerde(serdeConfig);
    final KStream<Long, Rating> ratingKStream = streamsApp.ratingsStream(builder, RATINGS_TOPIC_NAME, ratingSerde);

    SpecificAvroSerde<CountAndSum> countAndSumSerde = getSpecificAvroSerde(serdeConfig);
    final KTable<Long, Double>
        ratingAverageTable =
        streamsApp.ratingAverageTable(ratingKStream, AVG_RATINGS_TOPIC_NAME, countAndSumSerde);

    SpecificAvroSerde<Movie> movieSerde = getSpecificAvroSerde(serdeConfig);
    final KTable<Long, Movie> moviesTable = streamsApp.moviesTable(builder,
                                                                   MOVIES_TOPIC_NAME,
                                                                   movieSerde);

    SpecificAvroSerde<RatedMovie> ratedMovieSerde = getSpecificAvroSerde(serdeConfig);
    streamsApp.ratedMoviesTable(moviesTable, ratingAverageTable, "rated-movies", ratedMovieSerde);

    final Topology topology = builder.build();
    log.info("Topology = \n{}", topology.describe());
    td = new TopologyTestDriver(topology, p);
  }

  @Test
  public void validateRatingForLethalWeapon() {

    SpecificAvroSerde<Rating> ratingSerde = getSpecificAvroSerde(serdeConfig);
    final TestInputTopic<Long, Rating> rawRatingsTopic =
        td.createInputTopic(RATINGS_TOPIC_NAME, new LongSerializer(), ratingSerde.serializer());

    SpecificAvroSerde<Movie> movieSerde = getSpecificAvroSerde(serdeConfig);
    final TestInputTopic<Long, Movie>
        rawMoviesTopic =
        td.createInputTopic(MOVIES_TOPIC_NAME, new LongSerializer(), movieSerde.serializer());

    rawMoviesTopic.pipeValueList(Collections.singletonList(Parser.parseMovie(LETHAL_WEAPON_MOVIE)));

    rawRatingsTopic
        .pipeValueList(Arrays.asList(Parser.parseRating(LETHAL_WEAPON_RATING_9),
                                     Parser.parseRating(LETHAL_WEAPON_RATING_9),
                                     Parser.parseRating(LETHAL_WEAPON_RATING_9)));

    SpecificAvroSerde<RatedMovie> ratedMovieSerde = getSpecificAvroSerde(serdeConfig);
    final TestOutputTopic<Long, RatedMovie>
        outputTopic =
        td.createOutputTopic(RATED_MOVIES_TOPIC_NAME, new LongDeserializer(), ratedMovieSerde.deserializer());
    final List<KeyValue<Long, RatedMovie>> result = outputTopic.readKeyValuesToList();

    result.forEach(record -> {
      ProducerRecord<Long, RatedMovie>
          lethalWeaponRecord =
          new ProducerRecord<>(RATED_MOVIES_TOPIC_NAME, 362L, new RatedMovie(362L, "Lethal Weapon", 1987, 9.0));

      Assertions.assertThat(record.value).isEqualTo(lethalWeaponRecord.value());
    });

  }

  @AfterEach
  public void tearDown() {
    td.close();
  }
}
