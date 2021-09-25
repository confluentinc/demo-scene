package io.confluent.developer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.confluent.developer.avro.Pizza;

@KafkaClient
public interface CheeseProducer {
    @Topic("pizza-with-cheese")
    void sendCheesedPizza(@KafkaKey String key, Pizza cheesedPizza);
}