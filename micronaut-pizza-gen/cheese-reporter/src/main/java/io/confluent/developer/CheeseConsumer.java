package io.confluent.developer;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import jakarta.inject.Inject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.confluent.developer.avro.Pizza;

import java.util.HashMap;
import java.util.Map;

@KafkaListener(offsetReset = OffsetReset.EARLIEST)
public class CheeseConsumer {

    @Inject
    private CheeseReport cheeseReport;

    @Topic("pizza-with-cheese")
    public void receive(ConsumerRecord<String, Pizza> record) {
        Pizza pizza = record.value();
        String cheese = pizza.getCheese();
        cheeseReport.addCheeseCount(cheese);
    }

}