package io.confluent.demo;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.extern.slf4j.Slf4j;

import static io.confluent.demo.StreamsDemo.RATED_MOVIES_TOPIC_NAME;
import static io.confluent.demo.StreamsDemo.RAW_MOVIES_TOPIC_NAME;
import static io.confluent.demo.StreamsDemo.RAW_RATINGS_TOPIC_NAME;
import static io.confluent.demo.StreamsDemo.getMoviesTable;
import static io.confluent.demo.StreamsDemo.getRatedMoviesTable;
import static io.confluent.demo.StreamsDemo.getRatingAverageTable;
import static io.confluent.demo.StreamsDemo.getRawRatingsStream;
import static io.confluent.demo.StreamsDemo.getSerdeConfig;
import static io.confluent.demo.StreamsDemo.getStreamsConfig;
import static io.confluent.demo.fixture.MoviesAndRatingsData.DUMMY_KAFKA_CONFLUENT_CLOUD_9092;
import static io.confluent.demo.fixture.MoviesAndRatingsData.DUMMY_SR_CONFLUENT_CLOUD_8080;
import static io.confluent.demo.fixture.MoviesAndRatingsData.LETHAL_WEAPON_MOVIE;
import static io.confluent.demo.fixture.MoviesAndRatingsData.LETHAL_WEAPON_RATING_9;
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.util.Collections.singletonMap;

@Slf4j
public class StreamsDemoE2ETest {

  private TopologyTestDriver td;
  private SpecificAvroSerde<RatedMovie> ratedMovieSerde;


  @Before
  public void setUp() {

    final Properties config = getStreamsConfig(DUMMY_KAFKA_CONFLUENT_CLOUD_9092, DUMMY_SR_CONFLUENT_CLOUD_8080,
                                               "");
    // workaround https://stackoverflow.com/a/50933452/27563
    config.setProperty(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams/" + LocalDateTime.now().toString());

    final Map<String, String> mockSerdeConfig = getSerdeConfig(config);

    //building topology
    StreamsBuilder builder = new StreamsBuilder();

    SpecificAvroSerde<Movie> movieSerde = new SpecificAvroSerde<>(new MockSchemaRegistryClient());
    movieSerde.configure(mockSerdeConfig, false);

    ratedMovieSerde = new SpecificAvroSerde<>(new MockSchemaRegistryClient());
    ratedMovieSerde.configure(mockSerdeConfig, false);

    final KStream<Long, String> rawRatingsStream = getRawRatingsStream(builder);
    SpecificAvroSerde<CountAndSum> countAndSumSerde = new SpecificAvroSerde<>(new MockSchemaRegistryClient());
    countAndSumSerde.configure(singletonMap(SCHEMA_REGISTRY_URL_CONFIG, DUMMY_SR_CONFLUENT_CLOUD_8080), false);
    final KTable<Long, Double> ratingAverageTable = getRatingAverageTable(rawRatingsStream, countAndSumSerde);

    final KTable<Long, Movie> moviesTable = getMoviesTable(builder, movieSerde);
    final KTable<Long, RatedMovie>
        ratedMoviesTable =
        getRatedMoviesTable(moviesTable, ratingAverageTable, ratedMovieSerde);

    final Topology topology = builder.build();
    log.info("Topology = \n{}", topology.describe());
    td = new TopologyTestDriver(topology, config);
  }

  @Test
  public void validateRatingForLethalWeapon() {

    ConsumerRecordFactory<Long, String> rawRatingsRecordFactory =
        new ConsumerRecordFactory<>(RAW_RATINGS_TOPIC_NAME, new LongSerializer(), new StringSerializer());

    ConsumerRecordFactory<Long, String> rawMoviesRecordFactory =
        new ConsumerRecordFactory<>(RAW_MOVIES_TOPIC_NAME, new LongSerializer(), new StringSerializer());

    td.pipeInput(rawMoviesRecordFactory.create(LETHAL_WEAPON_MOVIE));

    List<ConsumerRecord<byte[], byte[]>> list = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      list.add(rawRatingsRecordFactory.create(LETHAL_WEAPON_RATING_9));
    }
    td.pipeInput(list);

    List<ProducerRecord<Long, RatedMovie>> result = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      result.add(td.readOutput(RATED_MOVIES_TOPIC_NAME, new LongDeserializer(), ratedMovieSerde.deserializer()));
    }
    result.forEach(record -> {
      ProducerRecord<Long, RatedMovie>
          lethalWeaponRecord =
          new ProducerRecord<>(RATED_MOVIES_TOPIC_NAME, 362L, new RatedMovie(362L, "Lethal Weapon", 1987, 9.0));
      OutputVerifier.compareValue(record, lethalWeaponRecord);
    });


  }

}
