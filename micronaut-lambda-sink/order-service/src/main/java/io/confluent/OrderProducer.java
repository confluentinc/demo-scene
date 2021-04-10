package io.confluent;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

@KafkaClient
public interface OrderProducer {

    @Topic("new-orders")
    void sendOrder(@KafkaKey String key, NewOrder newOrder);

}