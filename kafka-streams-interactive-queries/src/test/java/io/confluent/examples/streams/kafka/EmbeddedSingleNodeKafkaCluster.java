/*
 * Copyright Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.confluent.examples.streams.kafka;

import io.confluent.kafka.schemaregistry.RestApp;
import io.confluent.kafka.schemaregistry.CompatibilityLevel;
import io.confluent.kafka.schemaregistry.rest.SchemaRegistryConfig;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.kafka.coordinator.group.GroupCoordinatorConfig;
import org.apache.kafka.network.SocketServerConfigs;
import org.apache.kafka.server.config.ServerConfigs;
import org.apache.kafka.server.config.ServerLogConfigs;
import org.apache.kafka.storage.internals.log.CleanerConfig;
import org.apache.kafka.test.TestCondition;
import org.apache.kafka.test.TestUtils;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Runs an in-memory, "embedded" Kafka cluster with 1 Kafka broker and 1 Confluent Schema Registry instance.
 */
public class EmbeddedSingleNodeKafkaCluster extends ExternalResource {

  private static final Logger log = LoggerFactory.getLogger(EmbeddedSingleNodeKafkaCluster.class);
  private static final String KAFKA_SCHEMAS_TOPIC = "_schemas";
  private static final String AVRO_COMPATIBILITY_TYPE = CompatibilityLevel.NONE.name;

  private static final String KAFKASTORE_OPERATION_TIMEOUT_MS = "60000";
  private static final String KAFKASTORE_DEBUG = "true";
  private static final String KAFKASTORE_INIT_TIMEOUT = "90000";

  private KafkaEmbedded broker;
  private Admin admin;
  private RestApp schemaRegistry;
  private final Map<String, String> brokerConfig;
  private boolean running;

  /**
   * Creates and starts the cluster.
   */
  public EmbeddedSingleNodeKafkaCluster() {
    this(new HashMap<>());
  }

  /**
   * Creates and starts the cluster.
   *
   * @param brokerConfig Additional broker configuration settings.
   */
  public EmbeddedSingleNodeKafkaCluster(final Map<String, String> brokerConfig) {
    this.brokerConfig = new HashMap<>();
    this.brokerConfig.put(SchemaRegistryConfig.KAFKASTORE_TIMEOUT_CONFIG, KAFKASTORE_OPERATION_TIMEOUT_MS);
    this.brokerConfig.putAll(brokerConfig);
  }

  /**
   * Creates and starts the cluster.
   */
  public void start() throws Exception {
    log.debug("Initiating embedded Kafka cluster startup");

    final Map<String, String> effectiveBrokerConfig = effectiveBrokerConfigFrom(brokerConfig);
    broker = new KafkaEmbedded(effectiveBrokerConfig);
    log.debug("Kafka instance is running at {}", broker.brokerList());

    final Properties adminConfig = new Properties();
    adminConfig.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, broker.brokerList());
    admin = Admin.create(adminConfig);

    final Properties schemaRegistryProps = new Properties();

    schemaRegistryProps.put(SchemaRegistryConfig.KAFKASTORE_TIMEOUT_CONFIG, KAFKASTORE_OPERATION_TIMEOUT_MS);
    schemaRegistryProps.put(SchemaRegistryConfig.DEBUG_CONFIG, KAFKASTORE_DEBUG);
    schemaRegistryProps.put(SchemaRegistryConfig.KAFKASTORE_INIT_TIMEOUT_CONFIG, KAFKASTORE_INIT_TIMEOUT);
    schemaRegistryProps.put(SchemaRegistryConfig.KAFKASTORE_BOOTSTRAP_SERVERS_CONFIG, broker.brokerList());

    schemaRegistry = new RestApp(0, null, KAFKA_SCHEMAS_TOPIC, AVRO_COMPATIBILITY_TYPE, schemaRegistryProps);
    schemaRegistry.start();
    running = true;
  }

  private Map<String, String> effectiveBrokerConfigFrom(final Map<String, String> brokerConfig) {
    final Map<String, String> effectiveConfig = new HashMap<>(brokerConfig);
    effectiveConfig.put(ServerConfigs.DELETE_TOPIC_ENABLE_CONFIG, "true");
    effectiveConfig.put(CleanerConfig.LOG_CLEANER_DEDUPE_BUFFER_SIZE_PROP, "" + 2 * 1024 * 1024L);
    effectiveConfig.put(GroupCoordinatorConfig.GROUP_MIN_SESSION_TIMEOUT_MS_CONFIG, "0");
    effectiveConfig.put(GroupCoordinatorConfig.OFFSETS_TOPIC_REPLICATION_FACTOR_CONFIG, "1");
    effectiveConfig.put(GroupCoordinatorConfig.OFFSETS_TOPIC_PARTITIONS_CONFIG, "1");
    effectiveConfig.put(ServerLogConfigs.AUTO_CREATE_TOPICS_ENABLE_CONFIG, "true");
    return effectiveConfig;
  }

  @Override
  protected void before() throws Exception {
    start();
  }

  @Override
  protected void after() {
    stop();
  }

  /**
   * Stops the cluster.
   */
  public void stop() {
    if (!running) {
      log.info("Confluent is already stopped");
      return;
    }
    log.info("Stopping Confluent");
    try {
      if (schemaRegistry != null) {
        schemaRegistry.stop();
      }
      if (broker != null) {
        broker.stop();
      }
    } catch (final Exception fatal) {
      throw new RuntimeException(fatal);
    } finally {
      running = false;
    }
    log.info("Confluent Stopped");
  }

  /**
   * This cluster's `bootstrap.servers` value.  Example: `127.0.0.1:9092`.
   * <p>
   * You can use this to tell Kafka Streams applications, Kafka producers, and Kafka consumers (new
   * consumer API) how to connect to this cluster.
   */
  public String bootstrapServers() {
    return broker.brokerList();
  }

  /**
   * The "schema.registry.url" setting of the schema registry instance.
   */
  public String schemaRegistryUrl() {
    return schemaRegistry.restConnect;
  }

  /**
   * Creates a Kafka topic with 1 partition and a replication factor of 1.
   *
   * @param topic The name of the topic.
   */
  public void createTopic(final String topic) throws InterruptedException {
    createTopic(topic, 1, (short) 1, Collections.emptyMap());
  }

  /**
   * Creates a Kafka topic with the given parameters.
   *
   * @param topic       The name of the topic.
   * @param partitions  The number of partitions for this topic.
   * @param replication The replication factor for (the partitions of) this topic.
   */
  public void createTopic(final String topic, final int partitions, final short replication) throws InterruptedException {
    createTopic(topic, partitions, replication, Collections.emptyMap());
  }

  /**
   * Creates a Kafka topic with the given parameters.
   *
   * @param topic       The name of the topic.
   * @param partitions  The number of partitions for this topic.
   * @param replication The replication factor for (partitions of) this topic.
   * @param topicConfig Additional topic-level configuration settings.
   */
  public void createTopic(final String topic,
                          final int partitions,
                          final short replication,
                          final Map<String, String> topicConfig) throws InterruptedException {
    createTopic(60000L, topic, partitions, replication, topicConfig);
  }

  /**
   * Creates a Kafka topic with the given parameters and blocks until all topics got created.
   *
   * @param topic       The name of the topic.
   * @param partitions  The number of partitions for this topic.
   * @param replication The replication factor for (partitions of) this topic.
   * @param topicConfig Additional topic-level configuration settings.
   */
  public void createTopic(final long timeoutMs,
                          final String topic,
                          final int partitions,
                          final short replication,
                          final Map<String, String> topicConfig) throws InterruptedException {
    broker.createTopic(topic, partitions, replication, topicConfig);

    if (timeoutMs > 0) {
      TestUtils.waitForCondition(new TopicCreatedCondition(topic), timeoutMs, "Topics not created after " + timeoutMs + " milli seconds.");
    }
  }

  /**
   * Deletes multiple topics and blocks until all topics got deleted.
   *
   * @param timeoutMs the max time to wait for the topics to be deleted (does not block if {@code <= 0})
   * @param topics    the name of the topics
   */
  public void deleteTopicsAndWait(final long timeoutMs, final String... topics) throws InterruptedException {
    for (final String topic : topics) {
      try {
        broker.deleteTopic(topic);
      } catch (final UnknownTopicOrPartitionException expected) {
        // indicates (idempotent) success
      }
    }

    if (timeoutMs > 0) {
      TestUtils.waitForCondition(new TopicsDeletedCondition(topics), timeoutMs, "Topics not deleted after " + timeoutMs + " milli seconds.");
    }
  }

  public boolean isRunning() {
    return running;
  }

  private final class TopicsDeletedCondition implements TestCondition {
    final Set<String> deletedTopics = new HashSet<>();

    private TopicsDeletedCondition(final String... topics) {
      Collections.addAll(deletedTopics, topics);
    }

    @Override
    public boolean conditionMet() {
      final ListTopicsResult result = admin.listTopics();
      try {
        final Set<String> topics = result.names().get();

        return topics.stream().noneMatch(deletedTopics::contains);
      } catch (final Exception error) {
        return false;
      }
    }
  }

  private final class TopicCreatedCondition implements TestCondition {
    final String createdTopic;

    private TopicCreatedCondition(final String topic) {
      createdTopic = topic;
    }

    @Override
    public boolean conditionMet() {
      final ListTopicsResult result = admin.listTopics();
      try {
        final Set<String> topics = result.names().get();

        return topics.contains(createdTopic);
      } catch (final Exception error) {
        return false;
      }
    }
  }

}
