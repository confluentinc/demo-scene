package io.confluent.demo;

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

import static io.confluent.demo.StreamsDemo.getMovieAvroSerde;
import static io.confluent.demo.StreamsDemo.getMoviesTable;
import static io.confluent.demo.StreamsDemo.getRatedMovieAvroSerde;
import static io.confluent.demo.StreamsDemo.getRatedMoviesTable;
import static io.confluent.demo.StreamsDemo.getRatingAverageTable;
import static io.confluent.demo.StreamsDemo.getRawRatingsStream;
import static io.confluent.demo.StreamsDemo.getSerdeConfig;
import static io.confluent.demo.StreamsDemo.getStreamsConfig;

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
  KTable ratedMoviesTable(StreamsBuilder builder, KafkaStreamsConfiguration kafkaStreamsConfiguration) {

    final Map<String, String> serdeConfig = getSerdeConfig(kafkaStreamsConfiguration.asProperties());

    final KStream<Long, String> rawRatingsStream = getRawRatingsStream(builder);
    SpecificAvroSerde<CountAndSum> countAndSumSerde = StreamsDemo.getCountAndSumSerde(serdeConfig);
    final KTable<Long, Double> ratingAverageTable = getRatingAverageTable(rawRatingsStream, countAndSumSerde);

    final KTable<Long, Movie> moviesTable = getMoviesTable(builder, getMovieAvroSerde(serdeConfig));
    return getRatedMoviesTable(moviesTable, ratingAverageTable, getRatedMovieAvroSerde(serdeConfig));
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
