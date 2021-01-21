package io.confluent.developer.ordersview;

import com.github.javafaker.Commerce;
import com.github.javafaker.Faker;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.lognet.springboot.grpc.context.LocalRunningGrpcPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Map;
import java.util.Random;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

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
    return TopicBuilder.name("orders").partitions(6).replicas(1).build();
  }

  @LocalRunningGrpcPort
  private int port;

  @Bean
  HostInfo getHostInfo() throws UnknownHostException {
    return new HostInfo(InetAddress.getLocalHost().getHostName(), port);
  }

  @Bean
  KafkaStreamsConfiguration defaultKafkaStreamsConfig(KafkaProperties configuredKafkaProperties)
      throws UnknownHostException {
    Map<String, Object> newConfig = configuredKafkaProperties.buildStreamsProperties();
    newConfig.put(
        StreamsConfig.APPLICATION_SERVER_CONFIG, InetAddress.getLocalHost().getHostName() + ":" + port);
    return new KafkaStreamsConfiguration(newConfig);
  }
}

@Component
class OrderView {

  @Autowired
  public void buildOrdersView(StreamsBuilder builder) {
    builder.table("orders",
                  Consumed.with(Serdes.Long(), Serdes.String()),
                  Materialized.as("orders-store"));
  }
}

@Component
@RequiredArgsConstructor
@Log4j2
class Producer {

  private final KafkaTemplate<Long, String> kafkaTemplate;

  @EventListener(ApplicationStartedEvent.class)
  public void produce() {
    final Random random = new Random();
    final Commerce commerceFaker = Faker.instance().commerce();
    Flux.interval(Duration.ofMillis(500))
        .map(aLong -> {
          final long round = Math.round(Math.abs(random.nextGaussian()) * 100);
          final String value = commerceFaker.productName();
          log.debug("key = {} : value = {}", round, value);
          return new ProducerRecord<>("orders", round, value);
        })
        .subscribe(kafkaTemplate::send);
  }
}

@RestController
@RequiredArgsConstructor
class MyIqController {

  private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

  @GetMapping("/iq/{id}")
  public String getOrder(@PathVariable final Long id) {
    final KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
    final ReadOnlyKeyValueStore<Long, String> store =
        kafkaStreams.store(fromNameAndType("orders-store", keyValueStore()));
    return store.get(id);
  }
}