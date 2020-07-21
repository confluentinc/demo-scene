package io.confluent.developer.livestreams;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
@EnableKafka
public class LivestreamsApplication {

  @Bean
  NewTopic livestreamsTopic() {
    return new NewTopic("livestreams", 3, (short) 3);
  }

  public static void main(String[] args) {
    SpringApplication.run(LivestreamsApplication.class, args);
  }

}


@Component
@RequiredArgsConstructor
class Producer {

  private final KafkaTemplate<Long, String> kafkaTemplate;

  @EventListener(ApplicationStartedEvent.class)
  public void produce() {
    for (long i = 0; i < 10; i++) {
      kafkaTemplate.send("livestreams", i, "livestreams").addCallback(result -> {
        if (result != null) {
          final long offset = result.getRecordMetadata().offset();
          System.out.println("offset = " + offset);
          final int partition = result.getRecordMetadata().partition();
          System.out.println("partition = " + partition);
        }

      }, ex -> System.err.println("not today"));
    }

  }
}

@Component
class Consumer {
  @KafkaListener(id = "myconsumer", topics = {"livestreams"})
  public void consumer(ConsumerRecord<Long, String> myRecord) {
    System.out.println("myRecord = " + myRecord.key() + ":" + myRecord.value());
  }
}