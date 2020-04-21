package io.confluent.developer.movies;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(initializers = AbstractIntegrationTest.Initializer.class)
class MoviesApplicationTests {

  @Test
  void contextLoads() {
  }

}
