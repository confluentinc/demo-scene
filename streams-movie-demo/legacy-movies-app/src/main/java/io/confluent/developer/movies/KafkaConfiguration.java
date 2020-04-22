package io.confluent.developer.movies;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfiguration {
  
  @Bean
  NewTopic movies() {
    return new NewTopic("movies", 10, (short) 1);
  }

  @Bean
  NewTopic ratings() {
    return new NewTopic("ratings", 10, (short) 1);
  }

  @Bean
  NewTopic ratedMovies() {
    return new NewTopic("rated-movies", 10, (short) 1);
  }

}
