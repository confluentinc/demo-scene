package io.confluent.demo;

import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Properties;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.slf4j.Slf4j;

import static io.confluent.demo.fixture.MoviesAndRatingsData.*;
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.*;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertNotNull;

@Slf4j
public class MoviesTopologyTest {

  TopologyTestDriver td;

  @Before
  public void setUp() {
    final Properties streamsConfig = StreamsDemo.getStreamsConfig(DUMMY_KAFKA_CONFLUENT_CLOUD_9092,
                                                                  DUMMY_SR_CONFLUENT_CLOUD_8080, "");

    // workaround https://stackoverflow.com/a/50933452/27563
    streamsConfig.setProperty(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams/" + LocalDateTime.now().toString());

    StreamsBuilder builder = new StreamsBuilder();

    SpecificAvroSerde<Movie> movieSerde = new SpecificAvroSerde<>(new MockSchemaRegistryClient());
    movieSerde.configure(singletonMap(SCHEMA_REGISTRY_URL_CONFIG, DUMMY_SR_CONFLUENT_CLOUD_8080), false);
    StreamsDemo.getMoviesTable(builder, movieSerde);

    final Topology topology = builder.build();
    log.info("Topology: \n{}", topology.describe());
    td = new TopologyTestDriver(topology, streamsConfig);
  }

  @Test
  public void validateIfTestDriverCreated() {
    assertNotNull(td);
  }

  @Test
  public void validateAvroMovie() {
    ConsumerRecordFactory<Long, String> rawMovieRecordFactory =
        new ConsumerRecordFactory<>(StreamsDemo.RAW_MOVIES_TOPIC_NAME, new LongSerializer(),
                                    new StringSerializer());

    td.pipeInput(rawMovieRecordFactory.create(LETHAL_WEAPON_MOVIE));

    //td.readOutput("")
    final KeyValueStore<Long, Movie> movieStore =
        td.getKeyValueStore("movies-store");
    final Movie movie = movieStore.get(362L);
    Assert.assertEquals("Lethal Weapon", movie.getTitle().toString());

  }
}
