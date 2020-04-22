package io.confluent.developer.movies;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MoviesApplication {

  // TODO: move to Kafka Configuration class (?)
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

  public static void main(String[] args) {
    SpringApplication.run(MoviesApplication.class, args);
  }
  
}
