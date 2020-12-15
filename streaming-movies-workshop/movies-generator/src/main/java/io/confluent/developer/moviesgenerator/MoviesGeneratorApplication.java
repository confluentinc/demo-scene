package io.confluent.developer.moviesgenerator;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static io.confluent.developer.moviesgenerator.Producer.MOVIES_TOPIC;
import static io.confluent.developer.moviesgenerator.Producer.RATINGS_TOPIC;

@SpringBootApplication
public class MoviesGeneratorApplication {

  public static void main(String[] args) {
    SpringApplication.run(MoviesGeneratorApplication.class, args);
  }

  @Value("${my.topics.replication.factor:1}")
  Short replicationFactor;

  @Value("${my.topics.partitions.count:1}")
  Integer partitions;

  @Bean
  NewTopic ratingsTopic() {
    return new NewTopic(RATINGS_TOPIC, partitions, replicationFactor);
  }

  @Bean
  NewTopic moviesTopic() {
    return new NewTopic(MOVIES_TOPIC, partitions, replicationFactor);
  }


}
