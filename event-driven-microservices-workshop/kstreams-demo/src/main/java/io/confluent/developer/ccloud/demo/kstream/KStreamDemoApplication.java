package io.confluent.developer.ccloud.demo.kstream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafkaStreams
@SpringBootApplication
public class KStreamDemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(KStreamDemoApplication.class, args);
  }
}
