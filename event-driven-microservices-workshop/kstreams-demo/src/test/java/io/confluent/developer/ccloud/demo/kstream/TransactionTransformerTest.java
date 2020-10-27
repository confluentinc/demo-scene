package io.confluent.developer.ccloud.demo.kstream;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.processor.MockProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.Before;
import org.junit.Test;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.UUID;

import io.confluent.developer.ccloud.demo.kstream.domain.Funds;
import io.confluent.developer.ccloud.demo.kstream.domain.Transaction;
import io.confluent.developer.ccloud.demo.kstream.domain.TransactionResult;

import static io.confluent.developer.ccloud.demo.kstream.KStreamConfigTest.testConfig;
import static io.confluent.developer.ccloud.demo.kstream.domain.TransactionResult.ErrorType.INSUFFICIENT_FUNDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class TransactionTransformerTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
  private KeyValueStore<String, Funds> fundsStore;
  private MockProcessorContext mockContext;
  private TransactionTransformer transactionTransformer;


  @Before
  public void setup() {
    final Properties properties = new Properties();
    properties.putAll(testConfig);
    mockContext = new MockProcessorContext(properties);

    fundsStore = Stores.keyValueStoreBuilder(
        Stores.inMemoryKeyValueStore("fundsStore"),
        Serdes.String(),
        new JsonSerde<>(Funds.class, OBJECT_MAPPER))
        .withLoggingDisabled()    // Changelog is not supported by MockProcessorContext.
        .build();

    fundsStore.init(mockContext, fundsStore);
    mockContext.register(fundsStore, null);

    transactionTransformer = new TransactionTransformer(fundsStore.name());
    transactionTransformer.init(mockContext);
  }

  @Test
  public void shouldStoreTransaction() {
    final Transaction
        transaction =
        new Transaction(UUID.randomUUID().toString(), "1", new BigDecimal(100), Transaction.Type.DEPOSIT, "USD", "USA");
    final TransactionResult transactionResult = transactionTransformer.transform(transaction);

    assertThat(transactionResult.isSuccess(), is(true));
  }

  @Test
  public void shouldHaveInsufficientFunds() {
    final Transaction
        transaction =
        new Transaction(UUID.randomUUID().toString(), "1", new BigDecimal("100"), Transaction.Type.WITHDRAW, "RUR",
                        "Russia");
    final TransactionResult result = transactionTransformer.transform(transaction);

    assertThat(result.isSuccess(), is(false));
    assertThat(result.getErrorType(), is(INSUFFICIENT_FUNDS));
  }

  @Test
  public void shouldHaveEnoughFunds() {
    final Transaction transaction1 =
        new Transaction(UUID.randomUUID().toString(), "1", new BigDecimal("300"), Transaction.Type.DEPOSIT, "RUR",
                        "Russia");

    final Transaction transaction2 =
        new Transaction(UUID.randomUUID().toString(), "1", new BigDecimal("200"), Transaction.Type.WITHDRAW, "RUR",
                        "Russia");
    transactionTransformer.transform(transaction1);
    final TransactionResult result = transactionTransformer.transform(transaction2);

    assertThat(result.isSuccess(), is(true));
    assertThat(result.getErrorType(), is(nullValue()));
  }
}