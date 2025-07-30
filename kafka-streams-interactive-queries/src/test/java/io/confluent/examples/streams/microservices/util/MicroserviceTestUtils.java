package io.confluent.examples.streams.microservices.util;

import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import io.confluent.examples.streams.kafka.EmbeddedSingleNodeKafkaCluster;

public class MicroserviceTestUtils {

  private static final Logger log = LoggerFactory.getLogger(MicroserviceTestUtils.class);

  @ClassRule
  public static final EmbeddedSingleNodeKafkaCluster CLUSTER = new EmbeddedSingleNodeKafkaCluster(
          new HashMap<>() {
            {
              //Transactions need durability so the defaults require multiple nodes.
              //For testing purposes set transactions to work with a single kafka broker.
              put("transaction.state.log.replication.factor", "1");
              put("transaction.state.log.min.isr", "1");
              put("transaction.state.log.num.partitions", "1");
            }
          });

  @AfterClass
  public static void stopCluster() {
    log.info("stopping cluster");
    if (CLUSTER.isRunning()) {
      CLUSTER.stop();
    }
  }

  protected static Properties producerConfig(final EmbeddedSingleNodeKafkaCluster cluster) {
    final Properties producerConfig = new Properties();
    producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, cluster.bootstrapServers());
    producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
    producerConfig.put(ProducerConfig.RETRIES_CONFIG, 1);
    return producerConfig;
  }

  public static <T> T getWithRetries(final Invocation.Builder builder,
                                     final GenericType<T> genericType,
                                     final int numberOfRetries) {
    final AtomicInteger retriesRemaining = new AtomicInteger(numberOfRetries);
    while (true) {
      try {
        return builder.get(genericType);
      } catch (final ServerErrorException exception) {
        if (exception.getMessage().contains("504") && retriesRemaining.getAndDecrement() > 0) {
          continue;
        }
        throw exception;
      }
    }
  }

  public static <T> T getWithRetries(final Invocation.Builder builder,
                                     final Class<T> clazz,
                                     final int numberOfRetries) {
    final AtomicInteger retriesRemaining = new AtomicInteger(numberOfRetries);
    while (true) {
      try {
        return builder.get(clazz);
      } catch (final ServerErrorException exception) {
        if (exception.getMessage().contains("504") && retriesRemaining.getAndDecrement() > 0) {
          continue;
        }
        throw exception;
      }
    }
  }

  public static <T> Response postWithRetries(final Invocation.Builder builder,
                                             final Entity<T> entity,
                                             final int numberOfRetries) {
    final AtomicInteger retriesRemaining = new AtomicInteger(numberOfRetries);
    while (true) {
      try {
        return builder.post(entity);
      } catch (final ServerErrorException exception) {
        if (exception.getMessage().contains("504") && retriesRemaining.getAndDecrement() > 0) {
          continue;
        }
        throw exception;
      }
    }
  }
}
