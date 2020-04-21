package io.confluent.developer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.slf4j.Slf4j;

import static io.confluent.developer.StreamsDemo.AVERAGE_RATINGS_TOPIC_NAME;
import static io.confluent.developer.StreamsDemo.RAW_RATINGS_TOPIC_NAME;
import static io.confluent.developer.StreamsDemo.getRatingAverageTable;
import static io.confluent.developer.StreamsDemo.getRawRatingsStream;
import static io.confluent.developer.StreamsDemo.getStreamsConfig;
import static io.confluent.developer.fixture.MoviesAndRatingsData.MOCK_SCHEMA_REGISTRY_URL_8080;
import static io.confluent.developer.fixture.MoviesAndRatingsData.LETHAL_WEAPON_RATING_10;
import static io.confluent.developer.fixture.MoviesAndRatingsData.LETHAL_WEAPON_RATING_8;
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertNotNull;

@Slf4j
public class RatingTopologiesTest {

  private TopologyTestDriver testDriver;

  @Before
  public void setUp() {
    final Properties streamsConfig = getStreamsConfig("dummy:1234", "mock://sr.url", "");
    StreamsBuilder builder = new StreamsBuilder();
    final KStream<Long, String> rawRatingsStream = getRawRatingsStream(builder);
    SpecificAvroSerde<CountAndSum> countAndSumSerde = StreamsDemo.getSpecificAvroSerde(streamsConfig);
    final KTable<Long, Double> ratingAverageTable = getRatingAverageTable(rawRatingsStream, countAndSumSerde);

    final Topology topology = builder.build();
    log.info("topology = \n" + topology.describe());
    testDriver = new TopologyTestDriver(topology, streamsConfig);

  }

  @Test
  public void validateIfTestDriverCreated() {
    assertNotNull(testDriver);
  }

  @Test
  public void validateAverageRating() {
    final ConsumerRecordFactory<Long, String>
        rawRatingRecordFactory =
        new ConsumerRecordFactory<>(RAW_RATINGS_TOPIC_NAME, new LongSerializer(),
                                    new StringSerializer());

    // Lethal Weapon ratings
    testDriver.pipeInput(Arrays.asList(rawRatingRecordFactory.create(LETHAL_WEAPON_RATING_10),
                                       rawRatingRecordFactory.create(LETHAL_WEAPON_RATING_8)));

    final ProducerRecord<Long, Double>
        o1 =
        testDriver.readOutput(AVERAGE_RATINGS_TOPIC_NAME, new LongDeserializer(),
                              new DoubleDeserializer());

    OutputVerifier.compareKeyValue(o1, 362L, 10.0);

    final KeyValueStore<Long, Double>
        keyValueStore =
        testDriver.getKeyValueStore("average-ratings");
    final Double expected = keyValueStore.get(362L);
    Assert.assertEquals("Message", expected, 9.0, 0.0);
  }

  @After
  public void tearDown() {
    testDriver.close();
  }
}