package io.confluent.developer.adventure;

import static io.confluent.developer.adventure.Constants.COMMANDS_STREAM;
import static io.confluent.developer.adventure.Constants.RESPONSES_STREAM;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

public class Database {
  private static Logger logger = LoggerFactory.getLogger(Database.class);

  public static void main(String[] args) {
    final String configFileName = "config/dev.properties";
    final Properties props;
    final Properties streamsConfiguration;

    // Config stuff.
    try {
      logger.info("Reading config file {}", configFileName);
      props = loadProperties(configFileName);
      streamsConfiguration = loadProperties(configFileName);
    } catch (IOException e) {
      logger.error("Cannot load config file: {} {}", configFileName, e);
      throw new RuntimeException(e);
    }

    final var builder = new StreamsBuilder();

    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    String schemaRegistryUrl = props.getProperty("schema.registry.url");
    Map<String, String> schemaRegistryProps =
        Collections.singletonMap(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

    // Serdes galore.
    var commandValueSerde = new SpecificAvroSerde<CommandValue>();
    commandValueSerde.configure(schemaRegistryProps, false);

    var responseValueSerde = new SpecificAvroSerde<ResponseValue>();
    responseValueSerde.configure(schemaRegistryProps, false);

    // Let's start some streams.
    var commandStream = builder.stream(COMMANDS_STREAM, Consumed.with(Serdes.UUID(), commandValueSerde));

    // Echo.
    commandStream.mapValues(command -> {
      var response = new ResponseValue();
      response.setSOURCE("Echo");
      response.setRESPONSE(String.format("ECHO: %s", command.getCOMMAND()));
      return response;
    }).to(RESPONSES_STREAM, Produced.with(Serdes.UUID(), responseValueSerde));

    // Command Parsing.
    InitialCommandProcessor.buildStreams(schemaRegistryProps, builder);

    // Position stream.
    KTable<UUID, LocationData> latestUserLocation = MovementProcessor.buildStreams(schemaRegistryProps, builder);

    // Inventory stuff.
    InventoryProcessor.buildStreams(schemaRegistryProps, builder, latestUserLocation);

    // Done. Build & ship.
    final Topology topology = builder.build();
    logger.info("Topology: {}", topology.describe());

    var streams = new KafkaStreams(topology, streamsConfiguration);
    streams.start();

    logger.info("Installing shutdown hook.");
    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }

  public static Properties loadProperties(String fileName) throws IOException {
    try (FileInputStream input = new FileInputStream(fileName)) {
      var props = new Properties();
      props.load(input);
      return props;
    }
  }
}
