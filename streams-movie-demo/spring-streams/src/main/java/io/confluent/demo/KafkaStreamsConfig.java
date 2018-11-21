package io.confluent.demo;

import static io.confluent.demo.StreamsDemo.*;
import static io.confluent.demo.StreamsDemo.getMovieAvroSerde;
import static io.confluent.demo.StreamsDemo.getRatedMoviesTable;
import static io.confluent.demo.StreamsDemo.getRatingAverageTable;
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.*;
import static java.util.Collections.singletonMap;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.util.Map;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaStreamsConfig {

  private static final int PARTITIONS = 1;

  private static final short REPLICAS = (short) 1;

  @Bean
  KTable ratedMoviesTable(StreamsBuilder builder, KafkaProperties kafkaProperties) {
    final KStream<Long, String> rawRatingsStream = getRawRatingsStream(builder);
    final KTable<Long, Double> ratingAverageTable = getRatingAverageTable(rawRatingsStream);

    final Map<String, String> serdeConfig = singletonMap(SCHEMA_REGISTRY_URL_CONFIG,
                                                         (String) kafkaProperties.buildStreamsProperties()
                                                             .get(SCHEMA_REGISTRY_URL_CONFIG));
    final KTable<Long, Movie> moviesTable = getMoviesTable(builder, getMovieAvroSerde(serdeConfig));
    return getRatedMoviesTable(moviesTable, ratingAverageTable, getRatedMovieAvroSerde(serdeConfig));
  }

  @Bean
  public NewTopic ratedMovies() {
    return new NewTopic("rated-movies", PARTITIONS, REPLICAS);
  }

  @Bean
  public NewTopic movies() {
    return new NewTopic("movies", PARTITIONS, REPLICAS);
  }

  @Bean
  public NewTopic rawMovies() {
    return new NewTopic("raw-movies", PARTITIONS, REPLICAS);
  }

  @Bean
  public NewTopic rawRatings() {
    return new NewTopic("raw-ratings", PARTITIONS, REPLICAS);
  }

  @Bean
  public NewTopic averageRatings() {
    return new NewTopic("average-ratings", PARTITIONS, REPLICAS);
  }

}
