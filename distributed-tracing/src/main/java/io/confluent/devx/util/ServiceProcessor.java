package io.confluent.devx.util;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import static java.lang.System.out;

@Service
public class ServiceProcessor {

  final private static String STAGE1 = "STAGE1";
  final private static Random RANDOM = new Random();

  final private KafkaTemplate<String, Model> kafkaTemplate;

  @Autowired
  public ServiceProcessor(final KafkaTemplate<String, Model> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Scheduled(fixedRate = 1000)
  public void publish() {
    kafkaTemplate.send(new ProducerRecord<>(STAGE1,
                                            new Model(RANDOM.nextInt(100), RANDOM.nextInt(100))));
  }

  @KafkaListener(topics = STAGE1)
  public void consume(ConsumerRecord<String, Model> record) {
    out.println(record.value());
  }

}

@Data
@RequiredArgsConstructor
@AllArgsConstructor
class Model {

  private int x;
  private int y;

  @Override
  public String toString() {

    return "{" + "x=" + x
           + ", y=" + y
           + '}';
  }
}