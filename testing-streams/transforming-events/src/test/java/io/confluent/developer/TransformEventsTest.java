package io.confluent.developer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import io.confluent.developer.avro.Movie;
import io.confluent.developer.avro.RawMovie;
import io.confluent.testcontainers.SchemaRegistryContainer;

import static java.time.Duration.ofMillis;

public class TransformEventsTest {

  private final static String TEST_CONFIG_FILE = "configuration/test.properties";
  private final static Properties ENVIRONMENT_PROPERTIES = loadEnvironmentProperties();

  @ClassRule
  public static KafkaContainer kafkaContainer =
      new KafkaContainer(ENVIRONMENT_PROPERTIES.getProperty("confluent.version"))
          .withNetwork(Network.newNetwork());

  @Rule
  public SchemaRegistryContainer schemaRegistryContainer =
      new SchemaRegistryContainer(ENVIRONMENT_PROPERTIES
                                      .getProperty("confluent.version"))
          .withKafka(kafkaContainer);

  private String inputTopic, outputTopic;
  private TransformationEngine transEngine;
  private KafkaProducer<String, Movie> movieProducer;
  private KafkaProducer<String, RawMovie> rawMovieProducer;
  private KafkaConsumer<String, RawMovie> rawMovieConsumer;
  private KafkaConsumer<String, Movie> outputConsumer;

  @Before
  public void initialize() {

    TransformEvents transformEvents = new TransformEvents();
    ENVIRONMENT_PROPERTIES.put("bootstrap.servers", kafkaContainer.getBootstrapServers());
    ENVIRONMENT_PROPERTIES.put("schema.registry.url", schemaRegistryContainer.getSchemaRegistryUrl());
    transformEvents.createTopics(ENVIRONMENT_PROPERTIES);

    inputTopic = ENVIRONMENT_PROPERTIES.getProperty("input.topic.name");
    outputTopic = ENVIRONMENT_PROPERTIES.getProperty("output.topic.name");
    Properties producerProps = transformEvents.buildProducerProperties(ENVIRONMENT_PROPERTIES);
    Properties inputConsumerProps = transformEvents.buildConsumerProperties("inputGroup", ENVIRONMENT_PROPERTIES);
    Properties outputConsumerProps = transformEvents.buildConsumerProperties("outputGroup", ENVIRONMENT_PROPERTIES);

    rawMovieProducer = transformEvents.createRawMovieProducer(producerProps);
    movieProducer = transformEvents.createMovieProducer(producerProps);
    rawMovieConsumer = transformEvents.createRawMovieConsumer(inputConsumerProps);
    outputConsumer = transformEvents.createMovieConsumer(outputConsumerProps);

  }

  @After
  public void tearDown() {
    transEngine.shutdown();
  }

  @Test
  public void checkIfYearFieldEndsUpSplitter() {

    List<RawMovie> input = new ArrayList<>();
    input.add(RawMovie.newBuilder().setId(294).setTitle("Die Hard::1988").setGenre("action").build());
    input.add(RawMovie.newBuilder().setId(354).setTitle("Tree of Life::2011").setGenre("drama").build());
    input.add(RawMovie.newBuilder().setId(782).setTitle("A Walk in the Clouds::1995").setGenre("romance").build());
    input.add(RawMovie.newBuilder().setId(128).setTitle("The Big Lebowski::1998").setGenre("comedy").build());

    List<Movie> expectedOutput = new ArrayList<>();
    expectedOutput
        .add(Movie.newBuilder().setTitle("Die Hard").setId(294).setReleaseYear(1988).setGenre("action").build());
    expectedOutput
        .add(Movie.newBuilder().setTitle("Tree of Life").setId(354).setReleaseYear(2011).setGenre("drama").build());
    expectedOutput.add(
        Movie.newBuilder().setTitle("A Walk in the Clouds").setId(782).setReleaseYear(1995).setGenre("romance")
            .build());
    expectedOutput.add(
        Movie.newBuilder().setTitle("The Big Lebowski").setId(128).setReleaseYear(1998).setGenre("comedy").build());

    transEngine = new TransformationEngine(inputTopic, outputTopic,
                                           rawMovieConsumer, movieProducer);

    Thread transEngineThread = new Thread(transEngine);
    List<Movie> actualOutput = null;

    try {
      transEngineThread.start();
      // Produce the raw movies for the testing process...
      produceRawMovies(inputTopic, input, rawMovieProducer);
      // Read the transformed records from the output topic,
      // that has been put there by the transformation engine.
      actualOutput = consumeMovies(outputTopic, outputConsumer);
    } finally {
      transEngine.shutdown();
    }

    Assert.assertEquals(expectedOutput, actualOutput);

  }

  private List<Movie> consumeMovies(String outputTopic,
                                    KafkaConsumer<String, Movie> consumer) {

    // Wait five seconds until all the records gets persisted, to
    // avoid a race condition between producers and consumers...
    try {
      Thread.sleep(5000);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    List<Movie> output = new ArrayList<>();
    consumer.subscribe(Collections.singletonList(outputTopic));
    ConsumerRecords<String, Movie> records = consumer.poll(ofMillis(1000));

    for (ConsumerRecord<String, Movie> record : records) {
      output.add(record.value());
    }

    return output;

  }

  private void produceRawMovies(String inputTopic, List<RawMovie> rawMovies,
                                KafkaProducer<String, RawMovie> producer) {

    ProducerRecord<String, RawMovie> record = null;
    for (RawMovie movie : rawMovies) {
      record = new ProducerRecord<>(inputTopic, movie);
      producer.send(record);
    }

  }

  private static Properties loadEnvironmentProperties() {

    Properties environmentProps = new Properties();
    try (FileInputStream input = new FileInputStream(TEST_CONFIG_FILE)) {
      environmentProps.load(input);
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return environmentProps;

  }

}