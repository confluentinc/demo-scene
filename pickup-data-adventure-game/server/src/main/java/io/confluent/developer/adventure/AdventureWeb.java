package io.confluent.developer.adventure;

import static io.confluent.developer.adventure.Constants.COMMANDS_STREAM;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ServerEndpoint("/socket/{user-id}")
public class AdventureWeb {
  private static Logger logger = LoggerFactory.getLogger(AdventureWeb.class);
  private Producer<String, CommandValue> producer;
  private final Properties props;
  private boolean connected = true;
  private Thread consumerThread;
  private ObjectMapper objectMapper;

  public AdventureWeb() throws IOException {
    objectMapper = new ObjectMapper();

    logger.info("Loading properties.");
    final String configFileName = "config/dev.properties";
    props = loadProperties(configFileName);

    final Properties producerProps = (Properties) props.clone();
    producer = new KafkaProducer<>(producerProps);
  }

  @OnOpen
  public void onOpen(Session session, @PathParam("user-id") String userId) throws IOException {
    logger.info("CONNECTED");
    Event<String, String> onOpenEventevent = new Event<String, String>("WebSocket", userId, "HELLO");
    sendEvent(session, onOpenEventevent);

    consumerThread = new Thread(() -> {
      final Properties consumerProps = (Properties) props.clone();
      consumerProps.setProperty("group.id", UUID.randomUUID().toString());

      try (Consumer<String, ResponseValue> consumer = new KafkaConsumer<>(consumerProps)) {
        consumer.subscribe(Collections.singletonList("responses"));
        while (connected) {
          try {
            ConsumerRecords<String, ResponseValue> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, ResponseValue> record : records) {
              logger.info("Got: {}", record);

              if (userId.equals(record.key())) {
                Event<String, String> event =
                    new Event<>(record.value().getSOURCE(), record.key(), record.value().getRESPONSE());
                sendEvent(session, event);
              }
            }
          } catch (JsonProcessingException e) {
            logger.error("Error while sending websocket message: {}", e);
          } catch (IOException e) {
            logger.error("Error while sending websocket message: {}", e);
          }
        }
      }
    });
    consumerThread.start();
  }

  @OnMessage
  public void onMessage(Session session, @PathParam("user-id") String userId, String message)
      throws JsonProcessingException {
    logger.info(" Got message: " + message);

    Event<String, CommandValue> event =
        objectMapper.readValue(message, new TypeReference<Event<String, CommandValue>>() {
        });
    logger.info("Got event: {}", event);

    ProducerRecord<String, CommandValue> record =
        new ProducerRecord<String, CommandValue>(COMMANDS_STREAM, userId, event.getValue());
    logger.info("Sending: {}", record);

    producer.send(record, (receipt, e) -> {
      if (e != null)
        logger.error("Error while producing event: {}", e);
      else
        logger.info("Produced event to topic {}: key = {} value = {}", COMMANDS_STREAM, receipt);
    });
  }

  @OnError
  public void onError(Session session, Throwable e) {
    try {
      logger.error("Error while handling message: {}", e);
      String errorMessage = String.format("{\"error\": \"%s\"}", e.toString());
      session.getBasicRemote().sendText(errorMessage);
    } catch (IOException ioe) {
      logger.error("Error while reporting error: {}", ioe);
    }
  };

  @OnClose
  public void onClose(Session session, CloseReason reason) {
    this.connected = false;
    this.consumerThread = null;
    producer.close();
    logger.info("DISCONNECTED: {}", reason.toString());
  }

  public static Properties loadProperties(String fileName) throws IOException {
    try (FileInputStream input = new FileInputStream(fileName)) {
      final Properties props = new Properties();
      props.load(input);
      return props;
    }
  }

  private void sendEvent(Session session, Event<?, ?> event) throws JsonProcessingException, IOException {
    String msg = objectMapper.writeValueAsString(event);
    logger.info("Sending: {}", msg);
    session.getBasicRemote().sendText(msg);
  }
}
