package io.confluent.devx.util;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MyApplication {

  public static void main(String[] args) {
    SpringApplication.run(MyApplication.class, args);
  }

  @Bean
  // kafka-topics --bootstrap-server localhost:9092 --create --topic STAGE1 --partitions 4 --replication-factor 1
  NewTopic newTopic() {
    return new NewTopic("STAGE1", 4, (short) 1);
  }
}