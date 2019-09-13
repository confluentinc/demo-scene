package io.confluent.devx.util;

import java.util.Random;

import com.google.gson.JsonObject;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ServiceProcessor {

    private static final String STAGE1 = "STAGE1";
    private static final Random RANDOM = new Random();

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedRate = 1000)
    public void createData() {

        JsonObject root = new JsonObject();
        root.addProperty("x", RANDOM.nextInt(100));
        root.addProperty("y", RANDOM.nextInt(100));
        String payload = root.toString();

        ProducerRecord<String, String> record =
            new ProducerRecord<String, String>(STAGE1,
                payload);

        kafkaTemplate.send(record);

    }

    @KafkaListener(topics = STAGE1)
    public void consume(ConsumerRecord<String, String> record) {

        System.out.println(record.value());

    }

}