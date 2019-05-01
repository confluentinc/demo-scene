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

import io.confluent.common.utils.TestUtils;
import io.confluent.kpay.util.KafkaTopicClient;
import io.confluent.kpay.util.KafkaTopicClientImpl;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class IntegrationTestHarness {

  private static final long TEST_RECORD_FUTURE_TIMEOUT_MS = 5000;

  public EmbeddedSingleNodeKafkaCluster embeddedKafkaCluster;
  private AdminClient adminClient;
  private KafkaTopicClient topicClient;
  Map<String, Object> configMap = new HashMap<>();
  String CONSUMER_GROUP_ID_PREFIX = "KWQ-test";

  public void start() throws Exception {
    embeddedKafkaCluster = new EmbeddedSingleNodeKafkaCluster();
    embeddedKafkaCluster.start();

    configMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaCluster.bootstrapServers());
    configMap.put("application.id", "KWQ");
    configMap.put("commit.interval.ms", 0);
    configMap.put("cache.max.bytes.buffering", 0);
    configMap.put("auto.offset.reset", "earliest");
    configMap.put(StreamsConfig.STATE_DIR_CONFIG, TestUtils.tempDirectory().getPath());

    this.adminClient = AdminClient.create(configMap);
    this.topicClient = new KafkaTopicClientImpl(adminClient);
  }

  public void stop() {
    this.topicClient.close();
    this.adminClient.close();
    this.embeddedKafkaCluster.stop();
  }

  public <V> void produceData(String topicName,
                              Map<String, V> recordsToPublish,
                              Serializer<V> valueSerializer,
                              Long timestamp)
          throws InterruptedException, TimeoutException, ExecutionException {

    createTopic(topicName, 1, 1);

    Properties producerConfig = properties();
    KafkaProducer<String, V> producer =
            new KafkaProducer<>(producerConfig, new StringSerializer(), valueSerializer);

    Map<String, RecordMetadata> result = new HashMap<>();
    for (Map.Entry<String, V> recordEntry : recordsToPublish.entrySet()) {
      Future<RecordMetadata> recordMetadataFuture = producer.send(buildRecord(topicName, timestamp, recordEntry));
      result.put(recordEntry.getKey(), recordMetadataFuture.get(TEST_RECORD_FUTURE_TIMEOUT_MS, TimeUnit.MILLISECONDS));
    }
    producer.close();

  }

  public void createTopic(String topicName, int numPartitions, int replicationFactor) {
    topicClient.createTopic(topicName, numPartitions, (short) replicationFactor);
  }

  private <V> ProducerRecord<String, V> buildRecord(String topicName,
                                                         Long timestamp,
                                                         Map.Entry<String, V> recordEntry) {
    return new ProducerRecord<>(topicName, null, timestamp,  recordEntry.getKey(), recordEntry.getValue());
  }

  private Properties properties() {
    Properties producerConfig = new Properties();
    producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            configMap.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
    producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
    producerConfig.put(ProducerConfig.RETRIES_CONFIG, 0);
    return producerConfig;
  }

  private long groupId = System.currentTimeMillis();

  public <K, V> Map<K, V> consumeData(String topic,
                                            int expectedNumMessages,
                                            Deserializer<K> keyDeserializer,
                                            Deserializer<V> valueDeserializer,
                                            long resultsPollMaxTimeMs) {

    Properties consumerConfig = new Properties();
      consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, configMap.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));

    consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID_PREFIX + groupId++);
    consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    System.out.println("IntTestHarness - ConsumerConfig: Topic:" + topic + " properties:" + consumerConfig);

    try (KafkaConsumer<K, V> consumer = new KafkaConsumer(consumerConfig,
            keyDeserializer,
            valueDeserializer)) {

      consumer.subscribe(Collections.singleton(topic));

      Map<K, V> result = new HashMap<>();

      int waitCount = 0;
      while (result.size() < expectedNumMessages && waitCount++ < 5) {
        for (ConsumerRecord<K, V> record : consumer.poll(resultsPollMaxTimeMs)) {
          if (record.value() != null) {
            result.put(record.key(), record.value());
          }
        }
      }

      return result;
    }
  }

  public KafkaTopicClient getTopicClient() {
    return topicClient;
  }
}
