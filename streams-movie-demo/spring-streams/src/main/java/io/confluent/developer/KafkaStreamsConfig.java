package io.confluent.developer;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.state.HostInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;

import static io.confluent.developer.StreamsDemo.getMoviesTable;
import static io.confluent.developer.StreamsDemo.getRatedMoviesTable;
import static io.confluent.developer.StreamsDemo.getRatingAverageTable;
import static io.confluent.developer.StreamsDemo.getRawRatingsStream;
import static io.confluent.developer.StreamsDemo.getSpecificAvroSerde;
import static io.confluent.developer.StreamsDemo.getStreamsConfig;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaStreamsConfig {

  @Value("${kafka.bootstrapServers}")
  private String bootstrapServer;

  @Value("${confluent.schemaRegistryUrl}")
  private String schemaRegistryUrl;

  @Value("${server.port}")
  private Integer port;

  @Bean
  KTable<Long, RatedMovie> ratedMoviesTable(StreamsBuilder builder,
                                            KafkaStreamsConfiguration kafkaStreamsConfiguration) {

    final Properties properties = kafkaStreamsConfiguration.asProperties();

    final KStream<Long, String> rawRatingsStream = getRawRatingsStream(builder);
    SpecificAvroSerde<CountAndSum> countAndSumSerde = getSpecificAvroSerde(properties);
    final KTable<Long, Double> ratingAverageTable = getRatingAverageTable(rawRatingsStream, countAndSumSerde);

    final KTable<Long, Movie> moviesTable = getMoviesTable(builder, getSpecificAvroSerde(properties));
    return getRatedMoviesTable(moviesTable, ratingAverageTable, getSpecificAvroSerde(properties));
  }

  @Bean
  KafkaStreamsConfiguration defaultKafkaStreamsConfig() throws UnknownHostException {
    final Properties
        streamsConfig =
        getStreamsConfig(bootstrapServer, schemaRegistryUrl, System.getProperty("user.home") + "/.ccloud/config");
    streamsConfig.put(StreamsConfig.APPLICATION_SERVER_CONFIG, InetAddress.getLocalHost().getHostName() + ":" + port);
    return new KafkaStreamsConfiguration((Map) streamsConfig);
  }

  @Bean
  HostInfo getHostInfo() throws UnknownHostException {
    return new HostInfo(InetAddress.getLocalHost().getHostName(), port);
  }
}
