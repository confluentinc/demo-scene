package io.confluent.developer.dashboard;

import static java.math.RoundingMode.HALF_DOWN;

import java.io.IOException;
import java.io.InputStream;
import java.math.*;
import java.util.*;

import org.apache.kafka.clients.producer.*;
import org.slf4j.*;

public class PurchaseGenerator {
  private static Logger logger = LoggerFactory.getLogger(PurchaseGenerator.class);

  private static Random random = new Random();

  public static void main(String[] args) throws IOException {
    final InputStream input = PurchaseGenerator.class.getResourceAsStream("/dev.properties");
    final Properties props = new Properties();
    props.load(input);
    input.close();

    Producer<String, Purchase> producer = new KafkaProducer<>(props);
    Runtime.getRuntime().addShutdownHook(new Thread(producer::close));

    List<String> skus = Arrays.asList("A0001", "A0002", "A0003", "B0004", "B0005", "B0004", "C0007", "C0008", "C0009");
    List<String> names = Arrays.asList("bear", "screwdriver", "cat", "laundry basket", "fresh fish");
    List<String> descriptions =
        Arrays
          .asList("Cute and fluffy.", "Useful around the home.", "A trained rat-catcher.",
              "Where else are you going to put it?", "Tasty - just cook it as soon as it arrives!");

    while (true) {
      String sku = oneOf(skus);
      String name = oneOf(names);
      String description = oneOf(descriptions);
      BigDecimal price = new BigDecimal(random.nextInt(1000) + 1).divide(new BigDecimal(100), 2, HALF_DOWN);
      Purchase purchase = new Purchase(name, description, price);

      ProducerRecord<String, Purchase> purchaseRecord = new ProducerRecord<>("purchases", sku, purchase);

      producer.send(purchaseRecord, (event, e) -> {
        if (e == null) {
          logger.info("Sent {}", purchaseRecord);
        } else {
          logger.error("Exception while sending {}", purchaseRecord, e);
        }
      });
      producer.flush();

      try {
        int delayTime = random.nextInt(20) * 100;
        Thread.sleep(delayTime);
      } catch (InterruptedException e) {
      }
    }
  }

  private static <T> T oneOf(List<T> elements) {
    int idx = random.nextInt(elements.size());
    return elements.get(idx);
  }
}
