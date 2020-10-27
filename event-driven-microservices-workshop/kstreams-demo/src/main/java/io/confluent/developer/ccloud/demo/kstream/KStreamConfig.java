package io.confluent.developer.ccloud.demo.kstream;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonSerde;

import io.confluent.developer.ccloud.demo.kstream.domain.Funds;
import io.confluent.developer.ccloud.demo.kstream.domain.Transaction;
import io.confluent.developer.ccloud.demo.kstream.domain.TransactionResult;
import io.confluent.developer.ccloud.demo.kstream.topic.FundsStoreConfig;
import io.confluent.developer.ccloud.demo.kstream.topic.TopicConfig;
import io.confluent.developer.ccloud.demo.kstream.topic.TransactionFailedTopicConfig;
import io.confluent.developer.ccloud.demo.kstream.topic.TransactionRequestTopicConfig;
import io.confluent.developer.ccloud.demo.kstream.topic.TransactionSuccessTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableKafkaStreams
@Slf4j
@RequiredArgsConstructor
public class KStreamConfig {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

  private final TransactionRequestTopicConfig transactionRequestConfiguration;
  private final TransactionSuccessTopicConfig transactionSuccessConfiguration;
  private final TransactionFailedTopicConfig transactionFailedConfiguration;
  private final FundsStoreConfig fundsStoreConfig;

  @Bean
  NewTopic transactionFailed(TransactionFailedTopicConfig topicConfig) {
    return createTopic(topicConfig);
  }

  @Bean
  NewTopic transactionSuccess(TransactionSuccessTopicConfig topicConfig) {
    return createTopic(topicConfig);
  }

  private NewTopic createTopic(TopicConfig topicConfig) {
    log.info("Creating topic {}...", topicConfig.getName());
    return TopicBuilder.name(topicConfig.getName())
        .partitions(topicConfig.getPartitions())
        .replicas(topicConfig.getReplicationFactor())
        .compact()
        .build();
  }

  @Bean
  public Topology topology(StreamsBuilder streamsBuilder) {
    streamsBuilder.addStateStore(
        Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(fundsStoreConfig.getName()),
                                    Serdes.String(), new JsonSerde<>(Funds.class, OBJECT_MAPPER)));

    defineStreams(streamsBuilder);

    Topology topology = streamsBuilder.build();

    log.trace("Topology description : {}", topology.describe());

    return topology;
  }

  protected void defineStreams(StreamsBuilder streamsBuilder) {

    // TODO: implement me!!!
  }

  private boolean success(String account, TransactionResult result) {
    return result.isSuccess();
  }


}
