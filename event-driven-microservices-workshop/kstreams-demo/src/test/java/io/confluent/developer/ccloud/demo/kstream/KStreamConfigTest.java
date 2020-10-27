package io.confluent.developer.ccloud.demo.kstream;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.Before;
import org.junit.Test;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import io.confluent.developer.ccloud.demo.kstream.domain.Funds;
import io.confluent.developer.ccloud.demo.kstream.domain.Transaction;
import io.confluent.developer.ccloud.demo.kstream.domain.TransactionResult;
import io.confluent.developer.ccloud.demo.kstream.topic.FundsStoreConfig;
import io.confluent.developer.ccloud.demo.kstream.topic.TransactionFailedTopicConfig;
import io.confluent.developer.ccloud.demo.kstream.topic.TransactionRequestTopicConfig;
import io.confluent.developer.ccloud.demo.kstream.topic.TransactionSuccessTopicConfig;

import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class KStreamConfigTest {

  TopologyTestDriver testDriver;
  TransactionRequestTopicConfig txRequestConfig;
  TransactionSuccessTopicConfig txSuccessConfig;
  TransactionFailedTopicConfig txFailedTopicConfig;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
  private JsonSerde<Transaction> transactionSerde;
  private JsonSerde<TransactionResult> transactionResultSerde;
  private Serde<String> stringSerde;
  private Transaction deposit100;
  private Transaction withdraw100;
  private Transaction withdraw200;
  private FundsStoreConfig fundsStoreConfig;

  final static Map<String, String> testConfig = Map.of(
      BOOTSTRAP_SERVERS_CONFIG, "localhost:8080",
      APPLICATION_ID_CONFIG, "mytest",
      DEFAULT_KEY_SERDE_CLASS_CONFIG, "org.apache.kafka.common.serialization.Serdes$StringSerde",
      DEFAULT_VALUE_SERDE_CLASS_CONFIG, "org.springframework.kafka.support.serializer.JsonSerde",
      JsonDeserializer.TYPE_MAPPINGS, "transaction:io.confluent.developer.ccloud.demo.kstream.domain.Transaction",
      JsonDeserializer.TRUSTED_PACKAGES, "*"
  );
  private Properties properties;
  private Topology topology;

  @Before
  public void setUp() {

    // TODO use spring test and test configs
    txRequestConfig = new TransactionRequestTopicConfig();
    txRequestConfig.setName("transaction-request");

    txSuccessConfig = new TransactionSuccessTopicConfig();
    txSuccessConfig.setName("transaction-success");

    txFailedTopicConfig = new TransactionFailedTopicConfig();
    txFailedTopicConfig.setName("transaction-failed");

    fundsStoreConfig = new FundsStoreConfig();
    fundsStoreConfig.setName("funds-store");

    final StreamsBuilder streamsBuilder = new StreamsBuilder();
    topology = new KStreamConfig(txRequestConfig, txSuccessConfig, txFailedTopicConfig, fundsStoreConfig)
        .topology(streamsBuilder);

    // serdes
    transactionSerde = new JsonSerde<>(Transaction.class, OBJECT_MAPPER);
    transactionResultSerde = new JsonSerde<>(TransactionResult.class, OBJECT_MAPPER);
    stringSerde = Serdes.String();

    // ttd
    properties = new Properties();
    properties.putAll(testConfig);

    deposit100 = new Transaction(UUID.randomUUID().toString(),
                                 "1",
                                 new BigDecimal(100),
                                 Transaction.Type.DEPOSIT,
                                 "USD",
                                 "USA");

    withdraw100 = new Transaction(UUID.randomUUID().toString(),
                                  "1",
                                  new BigDecimal(100),
                                  Transaction.Type.WITHDRAW,
                                  "USD",
                                  "USA");

    withdraw200 = new Transaction(UUID.randomUUID().toString(),
                                  "1",
                                  new BigDecimal(200),
                                  Transaction.Type.WITHDRAW,
                                  "USD",
                                  "USA");
  }

  @Test
  public void testDriverShouldNotBeNull() {
    try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, properties)) {
      assertThat(testDriver, not(nullValue())
      );
    }
  }

  @Test
  public void shouldCreateSuccessfulTransaction() {

    try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, properties)) {

      final TestInputTopic<String, Transaction> inputTopic = testDriver
          .createInputTopic(txRequestConfig.getName(), stringSerde.serializer(), transactionSerde.serializer());

      inputTopic.pipeInput(deposit100.getAccount(), deposit100);
      inputTopic.pipeInput(withdraw100.getAccount(), withdraw100);

      final TestOutputTopic<String, TransactionResult>
          outputTopic =
          testDriver.createOutputTopic(txSuccessConfig.getName(), stringSerde.deserializer(),
                                       transactionResultSerde.deserializer());

      final List<TransactionResult> successfulTransactions = outputTopic.readValuesToList();
      // balance should be 0
      final TransactionResult transactionResult = successfulTransactions.get(1);
      assertThat(transactionResult.getFunds().getBalance(), is(new BigDecimal(0)));
    }
  }

  @Test
  public void shouldBeInsufficientFunds() {

    try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, properties)) {
      final TestInputTopic<String, Transaction> inputTopic = testDriver
          .createInputTopic(txRequestConfig.getName(), stringSerde.serializer(), transactionSerde.serializer());

      inputTopic.pipeInput(deposit100.getAccount(), deposit100);
      inputTopic.pipeInput(withdraw200.getAccount(), withdraw200);

      final TestOutputTopic<String, TransactionResult>
          failedResultOutputTopic =
          testDriver.createOutputTopic(txFailedTopicConfig.getName(), stringSerde.deserializer(),
                                       transactionResultSerde.deserializer());

      final TestOutputTopic<String, TransactionResult>
          successResultOutputTopic =
          testDriver.createOutputTopic(txSuccessConfig.getName(), stringSerde.deserializer(),
                                       transactionResultSerde.deserializer());

      final TransactionResult successfulDeposit100Result = successResultOutputTopic.readValuesToList().get(0);
      assertThat(successfulDeposit100Result.getFunds().getBalance(), is(new BigDecimal(100)));

      final List<TransactionResult> failedTransactions = failedResultOutputTopic.readValuesToList();
      // balance should be 0
      final TransactionResult transactionResult = failedTransactions.get(0);
      assertThat(transactionResult.getErrorType(), is(TransactionResult.ErrorType.INSUFFICIENT_FUNDS));
    }
  }

  @Test
  public void balanceShouldBe300() {
    try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, properties)) {
      final TestInputTopic<String, Transaction> inputTopic = testDriver
          .createInputTopic(txRequestConfig.getName(), stringSerde.serializer(), transactionSerde.serializer());

      inputTopic.pipeInput(deposit100.getAccount(), deposit100);
      inputTopic.pipeInput(deposit100.getAccount(), deposit100);
      inputTopic.pipeInput(deposit100.getAccount(), deposit100);

      final KeyValueStore<String, Funds> store = testDriver.getKeyValueStore(fundsStoreConfig.getName());

      assertThat(store.get("1").getBalance(), is(new BigDecimal(300)));
    }
  }
}