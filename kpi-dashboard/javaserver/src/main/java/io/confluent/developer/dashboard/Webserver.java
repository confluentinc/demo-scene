package io.confluent.developer.dashboard;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

import javax.websocket.*;
import javax.websocket.server.*;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;

@ServerEndpoint("/websocket")
public class Webserver {
  private boolean running = false;

  @OnOpen
  public void onOpen(Session session) {
    Thread consumerThread = new Thread(() -> {
      Properties props = new Properties();
      props.put("bootstrap.servers", "http://localhost:9092");
      props.put("schema.registry.url", "http://localhost:8081");

      props.put("key.deserializer", StringDeserializer.class);
      props.put("value.deserializer", KafkaAvroDeserializer.class);
      props.put("specific.avro.reader", true);

      props.put("group.id", UUID.randomUUID().toString());

      Consumer<String, DashboardValue> consumer = new KafkaConsumer<>(props);

      consumer.subscribe(Collections.singletonList("DASHBOARD"));

      while (running) {
        ConsumerRecords<String, DashboardValue> records = consumer.poll(Duration.ofMillis(200));

        for (ConsumerRecord<String, DashboardValue> record : records) {
          try {
            session.getBasicRemote().sendText(record.value().toString());
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }

      consumer.close();
    });

    running = true;
    consumerThread.start();
  }

  @OnClose
  public void onClose() {
    running = false;
  }
}
