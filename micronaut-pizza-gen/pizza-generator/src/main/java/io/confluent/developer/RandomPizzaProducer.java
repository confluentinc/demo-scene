package io.confluent.developer;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.confluent.developer.avro.Pizza;

@KafkaClient
public interface RandomPizzaProducer {
    @Topic("pizza")
    void buildPizza(@KafkaKey String key, Pizza newPizza);
}