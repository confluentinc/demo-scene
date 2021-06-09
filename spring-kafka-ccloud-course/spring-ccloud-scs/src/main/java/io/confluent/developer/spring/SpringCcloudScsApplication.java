package io.confluent.developer.spring;

import com.github.javafaker.Faker;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;

import static java.util.Arrays.asList;
import static org.apache.kafka.common.serialization.Serdes.Long;
import static org.apache.kafka.common.serialization.Serdes.String;
import static org.apache.kafka.streams.kstream.Materialized.as;

@SpringBootApplication
public class SpringCcloudScsApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringCcloudScsApplication.class, args);
  }

  @Bean
  Supplier<Message<String>> produceChuckNorris() {
    return () -> MessageBuilder
        .withPayload(Faker.instance().chuckNorris().fact())
       // .setHeader(KafkaHeaders.MESSAGE_KEY, 42)
        .build();
  }

  @Bean
  Consumer<Message<String>> consumeChuckNorris() {
    return s -> System.out.println("FACT: \u001B[3m «" + s.getPayload() + "\u001B[0m»");
  }


  @Bean
  public Function<KStream<String, String>, KStream<String, Long>> processWords() {
    return inputStream -> {
      final Serde<String> stringSerde = String();
      final KStream<String, Long> countsStream = inputStream
          .flatMapValues(value -> asList(value.toLowerCase().split("\\W+")))
          .map((key, value) -> new KeyValue<>(value, value))
          .groupByKey(Grouped.with(stringSerde, stringSerde))
          .count(as("word-count-state-store"))
          .toStream();
      countsStream.to("counts", Produced.with(stringSerde, Long()));
      return countsStream;
    };
  }
}

@RestController
@RequiredArgsConstructor
class IQRestController {

  private final InteractiveQueryService iqService;

  @GetMapping("/iq/count/{word}")
  public Long getCount(@PathVariable final String word) {
    final ReadOnlyKeyValueStore<String, Long> store =
        iqService.getQueryableStore("word-count-state-store", QueryableStoreTypes.keyValueStore());
    return store.get(word);
  }
}
