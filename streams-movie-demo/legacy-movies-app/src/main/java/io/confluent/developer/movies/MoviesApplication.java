package io.confluent.developer.movies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MoviesApplication {

  public static void main(String[] args) {
    createApplication().run(args);

  }

  private static SpringApplication createApplication() {
    final SpringApplication application = new SpringApplication(MoviesApplication.class);
    // TODO: add initializers like 
    //application.addInitializers();
    return application;
  }
}
