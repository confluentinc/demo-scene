package io.confluent.consumer.demo;

import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.kafka.annotation.KafkaListener;

import static org.springframework.boot.Banner.Mode.OFF;
import static org.springframework.boot.WebApplicationType.NONE;


@SpringBootApplication
public class ConsoleConsumer {

  public static void main(String[] args) {
    new SpringApplicationBuilder(ConsoleConsumer.class)
        .web(NONE)
        .bannerMode(OFF)
        .run(args);
  }

  @KafkaListener(topics = "raw-movies", groupId = "spring")
  public void listen(String in, Consumer<Long, String> consumer) {
    System.out.println(in);
  }

}
