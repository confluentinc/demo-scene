package io.confluent.demo.naive;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class StreamProcessingUsingConsumerProducer {

  public static void main(String[] args) {

    int counter = 0;
    int sendInterval = 15;

    // in-memory store, not persistent
    Map<String, Integer> groupByCounts = new HashMap<>();

    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties());
         KafkaProducer<String, Integer> producer = new KafkaProducer<>(producerProperties())) {

      consumer.subscribe(Arrays.asList("A", "B"));

      while (true) { // consumer poll loop
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
        for (ConsumerRecord<String, String> record : records) {

          String key = record.key();
          Integer count = groupByCounts.get(key);

          if (count == null) {
            count = 0;
          }
          count += 1; // actually doing something useful

          groupByCounts.put(key, count);
        }

        if (counter++ % sendInterval == 0) {
          for (Map.Entry<String, Integer> groupedEntry : groupByCounts.entrySet()) {

            ProducerRecord<String, Integer> producerRecord =
                new ProducerRecord<>("group-by-counts", groupedEntry.getKey(), groupedEntry.getValue());
            producer.send(producerRecord);
          }
          consumer.commitSync();
        }
      }
    }
  }

  private static Properties producerProperties() {
    final Properties producerProperties = new Properties();
    return producerProperties;

  }

  private static Properties consumerProperties() {
    final Properties consumerProperties = new Properties();
    return consumerProperties;
  }
}