package io.confluent;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import io.micronaut.configuration.kafka.annotation.Topic;

@KafkaListener(offsetReset = OffsetReset.EARLIEST)
public class OrderConsumer {
    
    @Topic("<replace with success topic from connector>")
    public void receive(ConsumerRecord<String, Order> record) {
        System.out.println(record.value());
    }
}