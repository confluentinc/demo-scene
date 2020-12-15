package io.confluent.developer.ratingsprocessor;

import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import io.confluent.developer.Rating;
import io.confluent.developer.movies.serdes.MySerdes;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.slf4j.Slf4j;

import static io.confluent.developer.movies.util.Parser.parseRating;
import static io.confluent.developer.ratingsprocessor.fixture.MoviesAndRatingsData.DUMMY_KAFKA_CONFLUENT_CLOUD_9092;
import static io.confluent.developer.ratingsprocessor.fixture.MoviesAndRatingsData.DUMMY_SR_CONFLUENT_CLOUD_8080;
import static io.confluent.developer.ratingsprocessor.fixture.MoviesAndRatingsData.LETHAL_WEAPON_RATING_10;
import static io.confluent.developer.ratingsprocessor.fixture.MoviesAndRatingsData.LETHAL_WEAPON_RATING_8;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
public class RatingTopologyTest {

  private static final String RATINGS_TOPIC_NAME = "ratings";
  private static final String AVERAGE_RATINGS_TOPIC_NAME = "average-ratings";
  private TopologyTestDriver testDriver;

  final Map<String, String>
      serdeConfig =
      Map.of(SCHEMA_REGISTRY_URL_CONFIG, DUMMY_SR_CONFLUENT_CLOUD_8080);

  @BeforeEach
  public void setUp() {

    final Properties p = new Properties();
    p.put("application.id", "kafka-movies-test");
    p.put("bootstrap.servers", DUMMY_KAFKA_CONFLUENT_CLOUD_9092);
    p.put("schema.registry.url", DUMMY_SR_CONFLUENT_CLOUD_8080);
    p.put("default.topic.replication.factor", "1");
    p.put("offset.reset.policy", "latest");

    StreamsBuilder builder = new StreamsBuilder();

    final RatingProcessor streamsApp = new RatingProcessor(serdeConfig);

    final KStream<Long, Rating>
        ratingsStream =
        streamsApp.ratingsStream(builder, RATINGS_TOPIC_NAME, MySerdes.getSpecificAvroSerde(serdeConfig));

    final KTable<Long, Double>
        ratingAverageTable =
        streamsApp.ratingAverageTable(ratingsStream, "average-ratings", MySerdes.getSpecificAvroSerde(serdeConfig));

    final Topology topology = builder.build();
    log.info("topology = \n" + topology.describe());
    testDriver = new TopologyTestDriver(topology, p);

  }

  @Test
  public void validateIfTestDriverCreated() {
    assertThat(testDriver).isNotNull();
  }

  @Test
  public void validateAverageRating() {
    SpecificAvroSerde<Rating> ratingSerde = MySerdes.getSpecificAvroSerde(serdeConfig);
    final TestInputTopic<Long, Rating>
        inputTopic =
        testDriver.createInputTopic(RATINGS_TOPIC_NAME, new LongSerializer(), ratingSerde.serializer());

    // Lethal Weapon ratings
    inputTopic.pipeValueList(Arrays.asList(parseRating(LETHAL_WEAPON_RATING_10),
                                           parseRating(LETHAL_WEAPON_RATING_8)));

    final TestOutputTopic<Long, Double> outputTopic =
        testDriver.createOutputTopic(AVERAGE_RATINGS_TOPIC_NAME, new LongDeserializer(), new DoubleDeserializer());
    final KeyValue<Long, Double> longDoubleKeyValue = outputTopic.readKeyValuesToList().get(1);
    assertThat(longDoubleKeyValue).isEqualTo(new KeyValue<>(362L, 9.0));

    final KeyValueStore<Long, Double> keyValueStore = testDriver.getKeyValueStore("average-ratings");
    final Double expected = keyValueStore.get(362L);

    assertThat(expected).isEqualTo(9.0);
  }

  @AfterEach
  public void tearDown() {
    testDriver.close();
  }
}