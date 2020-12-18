package io.confluent.developer.ratingsprocessor;

import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;

import io.confluent.developer.Movie;
import io.confluent.developer.movies.serdes.MySerdes;
import io.confluent.developer.movies.util.Parser;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.slf4j.Slf4j;

import static io.confluent.developer.ratingsprocessor.fixture.MoviesAndRatingsData.DUMMY_KAFKA_CONFLUENT_CLOUD_9092;
import static io.confluent.developer.ratingsprocessor.fixture.MoviesAndRatingsData.DUMMY_SR_CONFLUENT_CLOUD_8080;
import static io.confluent.developer.ratingsprocessor.fixture.MoviesAndRatingsData.LETHAL_WEAPON_MOVIE;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class MoviesTopologyTest {

  private static final String MOVIES_TOPIC_NAME = "movies";
  private TopologyTestDriver td;

  final Map<String, String>
      serdeConfig =
      Map.of(SCHEMA_REGISTRY_URL_CONFIG, DUMMY_SR_CONFLUENT_CLOUD_8080);

  @BeforeEach
  public void setUp() throws IOException {

    final String tempDirectory = Files.createTempDirectory("kafka-streams")
        .toAbsolutePath()
        .toString();
    final Properties p = new Properties();
    p.put("application.id", "kafka-movies-test");
    p.put("bootstrap.servers", DUMMY_KAFKA_CONFLUENT_CLOUD_9092);
    p.put("schema.registry.url", DUMMY_SR_CONFLUENT_CLOUD_8080);
    p.put("default.topic.replication.factor", "1");
    p.put("offset.reset.policy", "latest");
    p.put(StreamsConfig.STATE_DIR_CONFIG, tempDirectory);

    StreamsBuilder builder = new StreamsBuilder();

    final RatingProcessor streamsApp = new RatingProcessor(serdeConfig);

    streamsApp.moviesTable(builder,
                           MOVIES_TOPIC_NAME,
                           MySerdes.getSpecificAvroSerde(serdeConfig));

    final Topology topology = builder.build();
    log.info("Topology: \n{}", topology.describe());
    td = new TopologyTestDriver(topology, p);
  }

  @Test
  public void validateIfTestDriverCreated() {
    assertNotNull(td);
  }

  @Test
  public void validateAvroMovie() {
    SpecificAvroSerde<Movie> moviesSerde = MySerdes.getSpecificAvroSerde(serdeConfig);
    final TestInputTopic<Long, Movie>
        inputTopic =
        td.createInputTopic(MOVIES_TOPIC_NAME, new LongSerializer(), moviesSerde.serializer());
    final Movie value = Parser.parseMovie(LETHAL_WEAPON_MOVIE);
    inputTopic.pipeInput(value.getMovieId(), value);

    final KeyValueStore<Long, Movie> movieStore = td.getKeyValueStore(MOVIES_TOPIC_NAME + "-store");
    final Movie movie = movieStore.get(362L);

    assertThat(movie.getTitle()).isEqualTo("Lethal Weapon");
  }
}
