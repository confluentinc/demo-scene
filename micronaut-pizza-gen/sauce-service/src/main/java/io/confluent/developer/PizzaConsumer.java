package io.confluent.developer;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import jakarta.inject.Inject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.confluent.developer.avro.Pizza;

@KafkaListener(offsetReset = OffsetReset.EARLIEST)
public class PizzaConsumer {
    @Inject
    private SauceService sauceService;

    @Inject
    private SauceProducer producer;

    @Topic("pizza")
    public void receive(ConsumerRecord<String, Pizza> record) {
        Pizza pizza = record.value();
        String orderId = record.key();
        System.out.println("Got Pizza - " + orderId);
        pizza.setSauce(sauceService.chooseSauce());
        producer.sendSaucedPizza(orderId, pizza);
    }
}