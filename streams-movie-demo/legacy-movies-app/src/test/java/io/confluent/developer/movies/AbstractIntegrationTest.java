package io.confluent.developer.movies;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.lifecycle.Startables;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import io.confluent.testcontainers.SchemaRegistryContainer;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = AbstractIntegrationTest.Initializer.class)
abstract class AbstractIntegrationTest {

  static class Initializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    // TODO
    //static MySQLContainer mysql = new MySQLContainer<>("debezium/example-mysql:0.9");
    static KafkaContainer kafka = new KafkaContainer("5.4.1");
    static SchemaRegistryContainer schemaRegistry = new SchemaRegistryContainer("5.4.1").withKafka(kafka);

    public static Map<String, String> getProperties() {
      //Startables.deepStart(Stream.of(mysql, kafka, schemaRegistry)).join();
      Startables.deepStart(Stream.of(kafka, schemaRegistry)).join();

      final Map<String, String> props = new HashMap<>();
      
      /*props.put("spring.datasource.url", mysql.getJdbcUrl());
      props.put("spring.datasource.username", mysql.getUsername());
      props.put("spring.datasource.password", mysql.getPassword());*/
      
      props.put("spring.kafka.properties.bootstrap.servers", kafka.getBootstrapServers());
      props.put("spring.kafka.properties.schema.registry.url", schemaRegistry.getTarget());
      
      return props;
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {
      final ConfigurableEnvironment env = context.getEnvironment();
      env.getPropertySources().addFirst(new MapPropertySource(
          "testcontainers", (Map) getProperties()
      ));
    }
  }
}