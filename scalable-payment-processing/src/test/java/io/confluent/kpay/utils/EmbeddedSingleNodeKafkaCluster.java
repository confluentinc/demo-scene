/**
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the GNU AFFERO GENERAL PUBLIC LICENSE, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/AGPL-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package io.confluent.kpay.utils;


import kafka.server.KafkaConfig$;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Runs an in-memory, "embedded" Kafka cluster with 1 ZooKeeper instance and 1 Kafka broker.
 */
public class EmbeddedSingleNodeKafkaCluster extends ExternalResource {

  private static final Logger log = LoggerFactory.getLogger(EmbeddedSingleNodeKafkaCluster.class);
  private static final int DEFAULT_BROKER_PORT = 0; // 0 results in a random port being selected

  private ZooKeeperEmbedded zookeeper;
  private KafkaEmbedded broker;
  private final Properties brokerConfig;

  /**
   * Creates and starts a Kafka cluster.
   */
  public EmbeddedSingleNodeKafkaCluster() {
    this(new Properties());
  }

  /**
   * Creates and starts a Kafka cluster.
   *
   * @param brokerConfig Additional broker configuration settings.
   */
  private EmbeddedSingleNodeKafkaCluster(Properties brokerConfig) {
    this.brokerConfig = new Properties();
    this.brokerConfig.put("group.initial.rebalance.delay.ms", "500");
    this.brokerConfig.putAll(brokerConfig);
  }

  /**
   * Creates and starts a Kafka cluster.
   */
  public void start() throws Exception {
    log.debug("Initiating embedded Kafka cluster startup");
    log.debug("Starting a ZooKeeper instance...");
    zookeeper = new ZooKeeperEmbedded();
    log.debug("ZooKeeper instance is running at {}", zookeeper.connectString());

    Properties effectiveBrokerConfig = effectiveBrokerConfigFrom(brokerConfig, zookeeper);
    log.debug("Starting a Kafka instance on port {} ...",
            effectiveBrokerConfig.getProperty(KafkaConfig$.MODULE$.PortProp()));
    broker = new KafkaEmbedded(effectiveBrokerConfig);
    log.debug("Kafka instance is running at {}, connected to ZooKeeper at {}",
            broker.brokerList(), broker.zookeeperConnect());
  }

  private Properties effectiveBrokerConfigFrom(Properties brokerConfig, ZooKeeperEmbedded zookeeper) {
    Properties effectiveConfig = new Properties();
    effectiveConfig.putAll(brokerConfig);
    effectiveConfig.put(KafkaConfig$.MODULE$.ZkConnectProp(), zookeeper.connectString());
    effectiveConfig.put(KafkaConfig$.MODULE$.PortProp(), DEFAULT_BROKER_PORT);
    effectiveConfig.put(KafkaConfig$.MODULE$.DeleteTopicEnableProp(), true);
    effectiveConfig.put(KafkaConfig$.MODULE$.LogCleanerDedupeBufferSizeProp(), 2 * 1024 * 1024L);
    effectiveConfig.put(KafkaConfig$.MODULE$.OffsetsTopicReplicationFactorProp(), (short) 1);
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
   * Stop the Kafka cluster.
   */
  public void stop() {

    if (broker != null) {
      broker.stop();
    }
    try {
      if (zookeeper != null) {
        zookeeper.stop();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This cluster's `bootstrap.servers` value.  Example: `127.0.0.1:9092`.
   *
   * You can use this to tell Kafka producers how to connect to this cluster.
   */
  public String bootstrapServers() {
    return broker.brokerList();
  }


  /**
   * Create a Kafka topic with the given parameters.
   *
   * @param topic       The name of the topic.
   * @param partitions  The number of partitions for this topic.
   * @param replication The replication factor for (partitions of) this topic.
   * @param topicConfig Additional topic-level configuration settings.
   */
  private void createTopic(String topic,
                           int partitions,
                           int replication,
                           Properties topicConfig) {
    broker.createTopic(topic, partitions, replication, topicConfig);
  }

}