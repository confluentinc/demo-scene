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

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.server.KafkaConfig;
import kafka.server.KafkaConfig$;
import kafka.server.KafkaServer;
import kafka.utils.TestUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.common.network.ListenerName;
import org.apache.kafka.common.utils.SystemTime;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Runs an in-memory, "embedded" instance of a Kafka broker, which listens at `127.0.0.1:9092` by
 * default.
 *
 * Requires a running ZooKeeper instance to connect to.  By default, it expects a ZooKeeper instance
 * running at `127.0.0.1:2181`.  You can specify a different ZooKeeper instance by setting the
 * `zookeeper.connect` parameter in the broker's configuration.
 */
class KafkaEmbedded {

  private static final Logger log = LoggerFactory.getLogger(KafkaEmbedded.class);

  private static final String DEFAULT_ZK_CONNECT = "127.0.0.1:2181";
  private static final int DEFAULT_ZK_SESSION_TIMEOUT_MS = 10 * 1000;
  private static final int DEFAULT_ZK_CONNECTION_TIMEOUT_MS = 8 * 1000;

  private final Properties effectiveConfig;
  private final File logDir;
  private final TemporaryFolder tmpFolder;
  private final KafkaServer kafka;

  /**
   * Creates and starts an embedded Kafka broker.
   *
   * @param config Broker configuration settings.  Used to modify, for example, on which port the
   *               broker should listen to.  Note that you cannot change some settings such as
   *               `log.dirs`, `port`.
   */
  public KafkaEmbedded(Properties config) throws IOException {
    tmpFolder = new TemporaryFolder();
    tmpFolder.create();
    logDir = tmpFolder.newFolder();
    effectiveConfig = effectiveConfigFrom(config);

    KafkaConfig kafkaConfig = new KafkaConfig(effectiveConfig);
    log.debug("Starting embedded Kafka broker (with log.dirs={} and ZK ensemble at {}) ...",
            logDir, zookeeperConnect());
    kafka = TestUtils.createServer(kafkaConfig, new SystemTime());
    log.debug("Startup of embedded Kafka broker at {} completed (with ZK ensemble at {}) ...",
            brokerList(), zookeeperConnect());
  }

  private Properties effectiveConfigFrom(Properties initialConfig) {
    Properties effectiveConfig = new Properties();
    effectiveConfig.put(KafkaConfig$.MODULE$.BrokerIdProp(), 0);
    effectiveConfig.put(KafkaConfig$.MODULE$.HostNameProp(), "127.0.0.1");
    effectiveConfig.put(KafkaConfig$.MODULE$.PortProp(), "9092");
    effectiveConfig.put(KafkaConfig$.MODULE$.NumPartitionsProp(), 1);
    effectiveConfig.put(KafkaConfig$.MODULE$.AutoCreateTopicsEnableProp(), true);
    effectiveConfig.put(KafkaConfig$.MODULE$.MessageMaxBytesProp(), 1000000);
    effectiveConfig.put(KafkaConfig$.MODULE$.ControlledShutdownEnableProp(), true);

    effectiveConfig.putAll(initialConfig);
    effectiveConfig.setProperty(KafkaConfig$.MODULE$.LogDirProp(), logDir.getAbsolutePath());
    return effectiveConfig;
  }

  /**
   * This broker's `metadata.broker.list` value.  Example: `127.0.0.1:9092`.
   *
   * You can use this to tell Kafka producers and consumers how to connect to this instance.
   */
  public String brokerList() {
    ListenerName listenerName = kafka.config().advertisedListeners().apply(0).listenerName();
    return String.join(":", kafka.config().hostName(), Integer.toString(kafka.boundPort
            (listenerName)));
  }


  /**
   * The ZooKeeper connection string aka `zookeeper.connect`.
   */
  public String zookeeperConnect() {
    return effectiveConfig.getProperty("zookeeper.connect", DEFAULT_ZK_CONNECT);
  }

  /**
   * Stop the broker.
   */
  public void stop() {
    log.debug("Shutting down embedded Kafka broker at {} (with ZK ensemble at {}) ...",
            brokerList(), zookeeperConnect());
    kafka.shutdown();
    kafka.awaitShutdown();
    log.debug("Removing temp folder {} with logs.dir at {} ...", tmpFolder, logDir);
    tmpFolder.delete();
    log.debug("Shutdown of embedded Kafka broker at {} completed (with ZK ensemble at {}) ...",
            brokerList(), zookeeperConnect());
  }

  /**
   * Create a Kafka topic with the given parameters.
   *
   * @param topic       The name of the topic.
   * @param partitions  The number of partitions for this topic.
   * @param replication The replication factor for (partitions of) this topic.
   * @param topicConfig Additional topic-level configuration settings.
   */
  public void createTopic(String topic,
                          int partitions,
                          int replication,
                          Properties topicConfig) {
    log.debug("Creating topic { name: {}, partitions: {}, replication: {}, config: {} }",
            topic, partitions, replication, topicConfig);
    // Note: You must initialize the ZkClient with ZKStringSerializer.  If you don't, then
    // registerTopic() will only seem to work (it will return without error).  The topic will exist in
    // only ZooKeeper and will be returned when listing topics, but Kafka itself does not create the
    // topic.
    ZkClient zkClient = new ZkClient(
            zookeeperConnect(),
            DEFAULT_ZK_SESSION_TIMEOUT_MS,
            DEFAULT_ZK_CONNECTION_TIMEOUT_MS,
            ZKStringSerializer$.MODULE$);
    ZkUtils zkUtils = new ZkUtils(zkClient, new ZkConnection(zookeeperConnect()), false);
    AdminUtils.createTopic(zkUtils, topic, partitions, replication, topicConfig, RackAwareMode.Enforced$.MODULE$);
    zkClient.close();
  }

}