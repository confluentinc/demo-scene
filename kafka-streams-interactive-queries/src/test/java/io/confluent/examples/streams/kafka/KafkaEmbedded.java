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

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.kafka.common.test.KafkaClusterTestKit;
import org.apache.kafka.common.test.TestKitNodes;
import org.apache.kafka.server.config.ServerConfigs;
import org.apache.kafka.server.config.ServerLogConfigs;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Runs an in-memory, "embedded" instance of a Kafka broker, which listens at `127.0.0.1:9092` by
 * default.
 */
public class KafkaEmbedded {

  private static final Logger log = LoggerFactory.getLogger(KafkaEmbedded.class);
  private final File logDir;
  private final TemporaryFolder tmpFolder;
  private final KafkaClusterTestKit cluster;

  /**
   * Creates and starts an embedded Kafka broker.
   *
   * @param config Broker configuration settings.  Used to modify, for example, on which port the
   *               broker should listen to.  Note that you cannot change some settings such as
   *               `log.dirs`, `port`.
   */
  public KafkaEmbedded(final Map<String, String> config) throws IOException {
    tmpFolder = new TemporaryFolder();
    tmpFolder.create();
    logDir = tmpFolder.newFolder();

    final Map<String, String> brokerConfig = effectiveConfigFrom(config);

    log.debug("Starting embedded Kafka broker (with log.dirs={}) ...", logDir);
    try {
      final KafkaClusterTestKit.Builder clusterBuilder = new KafkaClusterTestKit.Builder(
        new TestKitNodes.Builder()
          .setCombined(true)
          .setNumBrokerNodes(1)
          .setPerServerProperties(Map.of(0, brokerConfig))
          .setNumControllerNodes(1)
          .build()
      );

      cluster = clusterBuilder.build();
      cluster.nonFatalFaultHandler().setIgnore(true);

      cluster.format();
      cluster.startup();
      cluster.waitForReadyBrokers();
    } catch (final Exception e) {
      throw new KafkaException("Failed to create test Kafka cluster", e);
    }
    log.debug("Startup of embedded Kafka broker at {} completed ...", brokerList());
  }

  private Map<String, String> effectiveConfigFrom(final Map<String, String> initialConfig) {
    final Map<String, String> effectiveConfig = new HashMap<>();
    effectiveConfig.put(ServerConfigs.BROKER_ID_CONFIG, "0");
    effectiveConfig.put(ServerLogConfigs.NUM_PARTITIONS_CONFIG, "1");
    effectiveConfig.put(ServerLogConfigs.AUTO_CREATE_TOPICS_ENABLE_CONFIG, "true");
    effectiveConfig.put(ServerConfigs.MESSAGE_MAX_BYTES_CONFIG, "1000000");
    effectiveConfig.put(ServerConfigs.CONTROLLED_SHUTDOWN_ENABLE_CONFIG, "true");

    effectiveConfig.putAll(initialConfig);
    effectiveConfig.put(ServerLogConfigs.LOG_DIR_CONFIG, logDir.getAbsolutePath());
    return effectiveConfig;
  }

  /**
   * This broker's `metadata.broker.list` value.  Example: `127.0.0.1:9092`.
   * <p>
   * You can use this to tell Kafka producers and consumers how to connect to this instance.
   */
  public String brokerList() {
    return cluster.bootstrapServers();
  }


  /**
   * Stop the broker.
   */
  public void stop() throws Exception {
    cluster.close();
    log.debug("Removing temp folder {} with logs.dir at {} ...", tmpFolder, logDir);
    tmpFolder.delete();
  }

  /**
   * Create a Kafka topic with 1 partition and a replication factor of 1.
   *
   * @param topic The name of the topic.
   */
  public void createTopic(final String topic) {
    createTopic(topic, 1, (short) 1, Collections.emptyMap());
  }

  /**
   * Create a Kafka topic with the given parameters.
   *
   * @param topic       The name of the topic.
   * @param partitions  The number of partitions for this topic.
   * @param replication The replication factor for (the partitions of) this topic.
   */
  public void createTopic(final String topic, final int partitions, final short replication) {
    createTopic(topic, partitions, replication, Collections.emptyMap());
  }

  /**
   * Create a Kafka topic with the given parameters.
   *
   * @param topic       The name of the topic.
   * @param partitions  The number of partitions for this topic.
   * @param replication The replication factor for (partitions of) this topic.
   * @param topicConfig Additional topic-level configuration settings.
   */
  public void createTopic(final String topic,
                          final int partitions,
                          final short replication,
                          final Map<String, String> topicConfig) {
    log.debug("Creating topic { name: {}, partitions: {}, replication: {}, config: {} }",
      topic, partitions, replication, topicConfig);

    final Properties properties = new Properties();
    properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList());

    try (final AdminClient adminClient = AdminClient.create(properties)) {
      final NewTopic newTopic = new NewTopic(topic, partitions, replication);
      newTopic.configs(topicConfig);
      adminClient.createTopics(Collections.singleton(newTopic)).all().get();
    } catch (final InterruptedException | ExecutionException fatal) {
      throw new RuntimeException(fatal);
    }

  }

  /**
   * Delete a Kafka topic.
   *
   * @param topic The name of the topic.
   */
  public void deleteTopic(final String topic) {
    log.debug("Deleting topic {}", topic);
    final Properties properties = new Properties();
    properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList());

    try (final AdminClient adminClient = AdminClient.create(properties)) {
      adminClient.deleteTopics(Collections.singleton(topic)).all().get();
    } catch (final InterruptedException e) {
      throw new RuntimeException(e);
    } catch (final ExecutionException e) {
      if (!(e.getCause() instanceof UnknownTopicOrPartitionException)) {
        throw new RuntimeException(e);
      }
    }
  }
}