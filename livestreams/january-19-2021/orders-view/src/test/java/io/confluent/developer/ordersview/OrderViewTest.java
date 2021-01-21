package io.confluent.developer.ordersview;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;

class OrderViewTest {

  @Test
  void shouldCreateMaterializedView() {

    final StreamsBuilder builder = new StreamsBuilder();

    new OrderView().buildOrdersView(builder);
    Properties config = new Properties();
    config.putAll(Map.of(APPLICATION_ID_CONFIG, "test-app",
                         BOOTSTRAP_SERVERS_CONFIG, "dummy:9092"));

    try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(builder.build(), config)) {
      final TestInputTopic<Integer, String> orders =
          topologyTestDriver.createInputTopic("orders",
                                              Serdes.Integer().serializer(),
                                              Serdes.String().serializer());

      orders.pipeInput(1, "iPhone");
      orders.pipeInput(2, "iPad");
      orders.pipeInput(1, "iPhone, AirPods");
      orders.pipeInput(2, "HomePod");

      final KeyValueStore<Integer, String> keyValueStore =
          topologyTestDriver.getKeyValueStore("orders-store");

      assertThat(keyValueStore.get(1)).isEqualTo("iPhone, AirPods");
      assertThat(keyValueStore.get(2)).isEqualTo("HomePod");
    }
  }
}