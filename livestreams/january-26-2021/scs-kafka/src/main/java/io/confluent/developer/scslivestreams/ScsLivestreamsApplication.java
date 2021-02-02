package io.confluent.developer.scslivestreams;

import com.github.javafaker.Commerce;
import com.github.javafaker.Faker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import lombok.extern.log4j.Log4j2;

@SpringBootApplication
@Log4j2
public class ScsLivestreamsApplication {

  public static void main(String[] args) {
    SpringApplication.run(ScsLivestreamsApplication.class, args);
  }

  // ╔═══════════╦═══════════════╦═════════════════════════════╗
  // ║    SCS    ║     Kafka     ║ Java (java.util.function.*) ║
  // ╠═══════════╬═══════════════╬═════════════════════════════╣
  // ║ Sink      ║ Producer      ║ Supplier                    ║
  // ║ Source    ║ Consumer      ║ Consumer                    ║
  // ║ Processor ║ Kafka Streams ║ Function                    ║
  // ╚═══════════╩═══════════════╩═════════════════════════════╝

  @Bean
  Supplier<Message<String>> getProducts() {
    return () -> {
      final Faker instance = Faker.instance();
      final String productCode = instance.code().ean13();
      final Commerce commerce = instance.commerce();
      return MessageBuilder
          .withPayload(commerce.productName())
          .setHeader(KafkaHeaders.MESSAGE_KEY, productCode.getBytes(StandardCharsets.UTF_8))
          .build();
    };
  }

  @Bean
  Consumer<Message<String>> consumeProducts() {
    return o -> {
      final String payload = o.getPayload();
      final String key =
          new String((byte[]) Objects.requireNonNull(o.getHeaders().get(KafkaHeaders.RECEIVED_MESSAGE_KEY)),
                     StandardCharsets.UTF_8);
      log.info("key = {}, value = {}", key, payload);
    };
  }
}
