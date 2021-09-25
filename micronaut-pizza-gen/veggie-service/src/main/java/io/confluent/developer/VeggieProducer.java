package io.confluent.developer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.confluent.developer.avro.Pizza;

@KafkaClient
public interface VeggieProducer {
    @Topic("pizza-with-veggies")
    void sendVeggiePizza(@KafkaKey String key, Pizza veggiePizza);
}