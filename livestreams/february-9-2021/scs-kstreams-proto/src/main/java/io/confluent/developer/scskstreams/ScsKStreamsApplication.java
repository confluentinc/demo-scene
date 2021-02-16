package io.confluent.developer.scskstreams;

import com.github.javafaker.Commerce;
import com.github.javafaker.Faker;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.function.Function;
import java.util.function.Supplier;

import io.confluent.developer.proto.OrdersProto;

@SpringBootApplication
public class ScsKStreamsApplication {

  public static final String ORDER_VIEW_STORE_NAME = "order-view-store";

  public static void main(String[] args) {
    SpringApplication.run(ScsKStreamsApplication.class, args);
  }

  @Bean
  NewTopic products() {
    return TopicBuilder.name("orders").partitions(8).replicas(3).build();
  }

  @Bean
  Supplier<Message<OrdersProto.Order>> getProducts() {
    return () -> {
      final Faker instance = Faker.instance();
      final String productCode = instance.code().ean13();
      final Commerce commerce = instance.commerce();
      final OrdersProto.Order product = OrdersProto.Order.newBuilder()
          .setOrderId(instance.random().nextLong())
          .setCode(productCode)
          .setProduct(commerce.productName()).build();
      return MessageBuilder
          .withPayload(product)
          .setHeader(KafkaHeaders.MESSAGE_KEY, product.getOrderId())
          .build();
    };
  }


  //@Bean
  public Function<KStream<String, String>, KTable<String, String>> materializedView() {
    return stream -> stream.toTable(Named.as("orders-view"),
                                    Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as(
                                        ORDER_VIEW_STORE_NAME)
                                        .withKeySerde(Serdes.String())
                                        .withValueSerde(Serdes.String()));
  }
}

//@RestController
class IQController {

  private final InteractiveQueryService iqService;

  @Autowired
  public IQController(final InteractiveQueryService iqService) {
    this.iqService = iqService;
  }

  @GetMapping("iq/view/{key}")
  public String getItem(@PathVariable final String key) {
    final ReadOnlyKeyValueStore<String, String>
        store =
        iqService.getQueryableStore("order-view-store", QueryableStoreTypes.keyValueStore());
    return store.get(key);
  }
}
