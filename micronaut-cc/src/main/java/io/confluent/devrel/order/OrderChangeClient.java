package io.confluent.devrel.order;

import io.confluent.devrel.event.OrderChangeEvent;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

@KafkaClient(id = "order-changes")
public interface OrderChangeClient {

    @Topic("order-changes-avro")
    void sendOrderChange(@KafkaKey Long id, OrderChangeEvent event);
}
