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
package io.confluent.examples.streams.interactivequeries;

import com.google.common.collect.Sets;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.confluent.examples.streams.IntegrationTestUtils;
import io.confluent.examples.streams.kafka.EmbeddedSingleNodeKafkaCluster;

import static io.confluent.examples.streams.interactivequeries.WordCountInteractiveQueriesExample.DEFAULT_HOST;
import static io.confluent.examples.streams.microservices.util.MicroserviceTestUtils.getWithRetries;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThrows;

/**
 * End-to-end integration test for {@link WordCountInteractiveQueriesExample}. Demonstrates
 * how you can programmatically query the REST API exposed by {@link WordCountInteractiveQueriesRestService}
 */
public class WordCountInteractiveQueriesExampleTest {

  @ClassRule
  public static final EmbeddedSingleNodeKafkaCluster CLUSTER = new EmbeddedSingleNodeKafkaCluster();
  private static final String WORD_COUNT = "interactive-queries-wordcount-example-word-count-repartition";
  private static final String WINDOWED_WORD_COUNT = "interactive-queries-wordcount-example-windowed-word-count-repartition";
  private static final String WORD_COUNT_OUTPUT = "interactive-queries-wordcount-example-word-count-changelog";
  private static final String WINDOWED_WORD_COUNT_OUTPUT = "interactive-queries-wordcount-example-windowed-word-count-changelog";

  private static final List<String> inputValues = Arrays.asList(
          "hello",
          "world",
          "world",
          "hello world",
          "all streams lead to kafka",
          "streams",
          "kafka streams");

  @Rule
  public final TemporaryFolder temp = new TemporaryFolder();
  private KafkaStreams kafkaStreams;
  private WordCountInteractiveQueriesRestService proxy;

  private static final Logger log = LoggerFactory.getLogger(WordCountInteractiveQueriesExampleTest.class);

  @BeforeClass
  public static void createTopicsAndProduceDataToInputTopics() throws Exception {
    CLUSTER.createTopic(WordCountInteractiveQueriesExample.TEXT_LINES_TOPIC, 2, (short) 1);
    // The next two topics don't need to be created as they would be auto-created
    // by Kafka Streams, but it just makes the test more reliable if they already exist
    // as creating the topics causes a rebalance which closes the stores etc. So it makes
    // the timing quite difficult...
    CLUSTER.createTopic(WORD_COUNT, 2, (short) 1);
    CLUSTER.createTopic(WINDOWED_WORD_COUNT, 2, (short) 1);

    // Produce sample data to the input topic before the tests starts.
    final Properties producerConfig = new Properties();
    producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
    producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
    producerConfig.put(ProducerConfig.RETRIES_CONFIG, 1);
    producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    IntegrationTestUtils.produceValuesSynchronously(
            WordCountInteractiveQueriesExample.TEXT_LINES_TOPIC,
            inputValues,
            producerConfig);
  }

  @After
  public void shutdown() throws Exception {
    if (kafkaStreams != null) {
      kafkaStreams.close();
    }

    if (proxy != null) {
      proxy.stop();
    }
  }
  @Test
  public void shouldDemonstrateInteractiveQueries() throws Exception {
    final String host = DEFAULT_HOST;
    final int port = IntegrationTestUtils.randomFreeLocalPort();
    final String baseUrl = "http://" + host + ":" + port + "/state";

    kafkaStreams = WordCountInteractiveQueriesExample.createStreams(
            createStreamConfig(CLUSTER.bootstrapServers(), port, "one", host));

    final CountDownLatch startupLatch = new CountDownLatch(1);
    kafkaStreams.setStateListener((newState, oldState) -> {
      if (newState == KafkaStreams.State.RUNNING && oldState != KafkaStreams.State.RUNNING) {
        startupLatch.countDown();
      }
    });
    kafkaStreams.start();

    assertTrue("streams failed to start within timeout", startupLatch.await(60, TimeUnit.SECONDS));

    bindHostAndStartRestProxy(port, host);

    if (proxy != null) {
      final Client client = ClientBuilder.newClient();

      // Create a request to fetch all instances of HostStoreInfo
      final Invocation.Builder allInstancesRequest = client
              .target(baseUrl + "/instances")
              .request(MediaType.APPLICATION_JSON_TYPE);
      final List<HostStoreInfo> hostStoreInfo = fetchHostInfo(allInstancesRequest);

      assertThat(hostStoreInfo, hasItem(
              new HostStoreInfo(host, port, Sets.newHashSet("word-count", "windowed-word-count"))
      ));

      // Create a request to fetch all instances with word-count
      final Invocation.Builder wordCountInstancesRequest = client
              .target(baseUrl + "/instances/word-count")
              .request(MediaType.APPLICATION_JSON_TYPE);
      final List<HostStoreInfo> wordCountInstances = fetchHostInfo(wordCountInstancesRequest);

      assertThat(wordCountInstances, hasItem(
              new HostStoreInfo(host, port, Sets.newHashSet("word-count", "windowed-word-count"))
      ));

      final Properties consumerConfig = new Properties();
      consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
      consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
      consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
      consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, "wait-for-output-consumer");
      consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
      IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived(consumerConfig, WORD_COUNT_OUTPUT, inputValues.size());
      IntegrationTestUtils.waitUntilMinKeyValueRecordsReceived(consumerConfig, WINDOWED_WORD_COUNT_OUTPUT, inputValues.size());

      // Fetch all key-value pairs from the word-count store
      final Invocation.Builder allRequest = client
              .target(baseUrl + "/keyvalues/word-count/all")
              .request(MediaType.APPLICATION_JSON_TYPE);

      final List<KeyValueBean> allValues = Arrays.asList(
              new KeyValueBean("all", 1L),
              new KeyValueBean("hello", 2L),
              new KeyValueBean("kafka", 2L),
              new KeyValueBean("lead", 1L),
              new KeyValueBean("streams", 3L),
              new KeyValueBean("to", 1L),
              new KeyValueBean("world", 3L));
      final List<KeyValueBean> all = fetchRangeOfValues(allRequest, allValues);
      assertThat(all, equalTo(allValues));

      // Fetch a range of key-value pairs from the word-count store
      final List<KeyValueBean> expectedRange = Arrays.asList(
              new KeyValueBean("all", 1L),
              new KeyValueBean("hello", 2L),
              new KeyValueBean("kafka", 2L));

      final Invocation.Builder request = client
              .target(baseUrl + "/keyvalues/word-count/range/all/kafka")
              .request(MediaType.APPLICATION_JSON_TYPE);
      final List<KeyValueBean> range = fetchRangeOfValues(request, expectedRange);

      assertThat(range, equalTo(expectedRange));

      // Find the instance of the Kafka Streams application that would have the key hello
      Invocation.Builder builder = client
              .target(baseUrl + "/instance/word-count/hello")
              .request(MediaType.APPLICATION_JSON_TYPE);
      final HostStoreInfo hostWithHelloKey = getWithRetries(builder, HostStoreInfo.class, 5);

      // Fetch the value for the key hello from the instance.
      builder = client
              .target("http://" + hostWithHelloKey.getHost() + ":" + hostWithHelloKey.getPort() + "/state/keyvalue/word-count/hello")
              .request(MediaType.APPLICATION_JSON_TYPE);
      final KeyValueBean result = getWithRetries(builder, KeyValueBean.class, 5);

      assertThat(result, equalTo(new KeyValueBean("hello", 2L)));

      // fetch windowed values for a key
      builder = client
              .target(baseUrl + "/windowed/windowed-word-count/streams/0/" + System.currentTimeMillis())
              .request(MediaType.APPLICATION_JSON_TYPE);
      final List<KeyValueBean> windowedResult = getWithRetries(builder, new GenericType<List<KeyValueBean>>() {}, 5);
      assertThat(windowedResult.size(), equalTo(1));
      final KeyValueBean keyValueBean = windowedResult.get(0);
      assertTrue(keyValueBean.getKey().startsWith("streams"));
      assertThat(keyValueBean.getValue(), equalTo(3L));
    } else {
      fail("Should fail demonstrating InteractiveQueries as the Rest Service failed to start.");
    }
  }

  private void bindHostAndStartRestProxy(final int port, final String host) throws InterruptedException {
    int count = 0;
    final int maxTries = 10;
    while (count <= maxTries) {
      try {
        // Starts the Rest Service on the provided host:port
        proxy = WordCountInteractiveQueriesExample.startRestProxy(kafkaStreams, host, port);
        break;
      } catch (final Exception ex) {
        log.error("Could not start Rest Service due to: " + ex);
      }

      Thread.sleep(1000);

      count++;
    }
  }

  @Test(expected = Exception.class)
  public void shouldThrowExceptionForInvalidHost() throws Exception {
    final int port = IntegrationTestUtils.randomFreeLocalPort();
    final String host = "someInvalidHost";

    kafkaStreams = WordCountInteractiveQueriesExample.createStreams(
            createStreamConfig(CLUSTER.bootstrapServers(), port, "one", host));

    final CountDownLatch startupLatch = new CountDownLatch(1);
    kafkaStreams.setStateListener((newState, oldState) -> {
      if (newState == KafkaStreams.State.RUNNING && oldState != KafkaStreams.State.RUNNING) {
        startupLatch.countDown();
      }
    });

    kafkaStreams.start();
    proxy = WordCountInteractiveQueriesExample.startRestProxy(kafkaStreams, host, port);
  }

  @Test
  public void shouldThrowBindExceptionForUnavailablePort() throws Exception {
    final int port = IntegrationTestUtils.randomFreeLocalPort();
    final String host = "localhost";

    kafkaStreams = WordCountInteractiveQueriesExample.createStreams(
            createStreamConfig(CLUSTER.bootstrapServers(), port, "one", host));

    final CountDownLatch startupLatch = new CountDownLatch(1);
    kafkaStreams.setStateListener((newState, oldState) -> {
      if (newState == KafkaStreams.State.RUNNING && oldState != KafkaStreams.State.RUNNING) {
        startupLatch.countDown();
      }
    });

    kafkaStreams.start();
    proxy = WordCountInteractiveQueriesExample.startRestProxy(kafkaStreams, host, port);
    // Binding to same port again will raise BindException.
    final IOException exception = assertThrows(
            IOException.class,
            () -> WordCountInteractiveQueriesExample.startRestProxy(kafkaStreams, host, port)
    );
    assertThat(exception.getCause(), instanceOf(BindException.class));
  }

  /**
   * We fetch these in a loop as they are the first couple of requests
   * directly after KafkaStreams.start(), so it can take some time
   * for the group to stabilize and all stores/instances to be available
   */
  private List<HostStoreInfo> fetchHostInfo(final Invocation.Builder request) throws InterruptedException {
    List<HostStoreInfo> hostStoreInfo = getWithRetries(request, new GenericType<List<HostStoreInfo>>(){}, 5);
    final long until = System.currentTimeMillis() + 60000L;
    while (hostStoreInfo.isEmpty() ||
            hostStoreInfo.get(0).getStoreNames().size() != 2 && System.currentTimeMillis() < until) {
      Thread.sleep(10L);
      hostStoreInfo = getWithRetries(request, new GenericType<List<HostStoreInfo>>() {}, 5);
    }
    return hostStoreInfo;
  }

  private List<KeyValueBean> fetchRangeOfValues(final Invocation.Builder request,
                                                final List<KeyValueBean> expectedResults) {
    List<KeyValueBean> results = new ArrayList<>();
    final long timeout = System.currentTimeMillis() + 10000L;
    while (!results.containsAll(expectedResults) && System.currentTimeMillis() < timeout) {
      try {
        results = getWithRetries(request, new GenericType<List<KeyValueBean>>() {}, 5);
      } catch (final NotFoundException e) {
        //
      }
    }
    Collections.sort(results, Comparator.comparing(KeyValueBean::getKey));
    return results;
  }

  private Properties createStreamConfig(final String bootStrap,
                                        final int port,
                                        final String stateDir,
                                        final String host) throws IOException {
    final Properties streamsConfiguration = new Properties();
    // Give the Streams application a unique name.  The name must be unique in the Kafka cluster
    // against which the application is run.
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "interactive-queries-wordcount-example");
    // Where to find Kafka broker(s).
    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrap);
    // The host:port the embedded REST proxy will run on
    streamsConfiguration.put(StreamsConfig.APPLICATION_SERVER_CONFIG, host + ":" + port);
    // The directory where the RocksDB State Stores will reside
    streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, temp.newFolder(stateDir).getPath());
    // Set the default key serde
    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    // Set the default value serde
    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "100");
    streamsConfiguration.put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, "0");
    return streamsConfiguration;
  }
}
