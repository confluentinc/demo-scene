package io.confluent.developer.scskstreams;

import com.github.javafaker.Faker;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class ScsKStreamsApplicationTest {


  @Test
  void createMaterializedView() {

    final StreamsBuilder streamsBuilder = new StreamsBuilder();
    final Serde<String> stringSerde = Serdes.String();

    final KStream<String, String>
        stream =
        streamsBuilder.stream("orders-view", Consumed.with(stringSerde, stringSerde));

    final Function<KStream<String, String>, KTable<String, String>>
        kStreamKTableFunction =
        new ScsKStreamsApplication().materializedView();
    final KTable<String, String> apply = kStreamKTableFunction.apply(stream);

    var properties = new Properties();
    properties.putAll(Map.of(
        StreamsConfig.APPLICATION_ID_CONFIG, "my-test",
        StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:9092"
    ));

    try (TopologyTestDriver testDriver = new TopologyTestDriver(streamsBuilder.build(), properties)) {
      var inputTopic =
          testDriver.createInputTopic("orders-view", stringSerde.serializer(), stringSerde.serializer());

      final var instance = Faker.instance();
      final String key1 = instance.code().ean13();
      final String key2 = instance.code().ean13();
      inputTopic.pipeInput(key1, "iPad");
      inputTopic.pipeInput(key1, "iPad, iPod, HomePod");
      
      final KeyValueStore<String, String>
          keyValueStore =
          testDriver.getKeyValueStore(ScsKStreamsApplication.ORDER_VIEW_STORE_NAME);

      
      assertThat(keyValueStore.get(key1)).isEqualTo("iPad, iPod, HomePod");

    }


  }
}