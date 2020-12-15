package io.confluent.developer.ratingsprocessor;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG;

@SpringBootApplication
@EnableKafkaStreams
public class RatingsProcessorApplication {

  public static void main(String[] args) {
    SpringApplication.run(RatingsProcessorApplication.class, args);
  }

  @Value("${spring.kafka.properties.schema.registry.url}")
  String srUrl;

  @Value("${spring.kafka.properties.basic.auth.credentials.source}")
  String crSource;

  @Value("${spring.kafka.properties.schema.registry.basic.auth.user.info}")
  String authUser;

  @Bean
  Map<String, String> serdeConfig() {
    return Map.of(SCHEMA_REGISTRY_URL_CONFIG, srUrl,
                  BASIC_AUTH_CREDENTIALS_SOURCE, crSource,
                  USER_INFO_CONFIG, authUser);
  }

  @Value("${my.topic.avg-ratings.name:average-ratings}")
  String avgRatingsTopicName;

  @Value("${my.topic.rated-movies.name:rated-movies}")
  String ratedMoviesTopicName;

  @Value("${my.topics.replication.factor:1}")
  Short replicationFactor;

  @Value("${my.topics.partitions.count:1}")
  Integer partitions;

  @Bean
  NewTopic averageRatings() {
    return TopicBuilder
        .name(avgRatingsTopicName)
        .replicas(replicationFactor)
        .partitions(partitions)
        .build();
  }

  @Bean
  NewTopic ratedMovies() {
    return TopicBuilder
        .name(ratedMoviesTopicName)
        .replicas(replicationFactor)
        .partitions(partitions)
        .build();
  }
}


