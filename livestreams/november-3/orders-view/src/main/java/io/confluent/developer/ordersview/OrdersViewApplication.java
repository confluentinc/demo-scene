package io.confluent.developer.ordersview;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import static org.apache.kafka.streams.StoreQueryParameters.fromNameAndType;
import static org.apache.kafka.streams.state.QueryableStoreTypes.keyValueStore;


@EnableKafka
@EnableKafkaStreams
@SpringBootApplication
public class OrdersViewApplication {

  public static void main(String[] args) {
    SpringApplication.run(OrdersViewApplication.class, args);
  }

  @Bean
  NewTopic ordersTopic() {
    return TopicBuilder.name("orders").partitions(6).replicas(3).build();
  }

}

@Component
class OrderView {

  @Autowired
  public void buildOrdersView(StreamsBuilder builder) {
    builder.table("orders",
                  Consumed.with(Serdes.Integer(), Serdes.String()),
                  Materialized.as("orders-store"));
  }
}

@Component
@RequiredArgsConstructor
class Producer {

  private final KafkaTemplate<Integer, String> kafkaTemplate;

  @EventListener(ApplicationStartedEvent.class)
  public void produce() {
    kafkaTemplate.send("orders", 1, "iPad");
    kafkaTemplate.send("orders", 2, "iPhone");
    kafkaTemplate.send("orders", 1, "iPad, Airpods");
    kafkaTemplate.send("orders", 2, "HomePod");
  }
}

@RestController
@RequiredArgsConstructor
class MyIqController {

  private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

  @GetMapping("/iq/{id}")
  public String getOrder(@PathVariable final Integer id) {
    final KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
    final ReadOnlyKeyValueStore<Integer, String> store =
        kafkaStreams.store(fromNameAndType("orders-store", keyValueStore()));
    return store.get(id);
  }
}