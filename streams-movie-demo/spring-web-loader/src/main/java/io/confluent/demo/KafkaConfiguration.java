package io.confluent.demo;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;
import java.util.Properties;

import static io.confluent.demo.StreamsDemo.getStreamsConfig;

@Configuration
@EnableKafka
public class KafkaConfiguration {


  @Value("${kafka.bootstrap-servers}")
  String bootstrapServers;

  @Value("${kafka.schema.registry.url}")
  String schemaRegistryUrl;

  @Bean
  public DefaultKafkaProducerFactory<Long, String> producerFactory() {
    final Properties config =
        getStreamsConfig(bootstrapServers, schemaRegistryUrl, System.getProperty("user.home") + "/.ccloud/config");
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    //config.put("schema.registry.url", schemaRegistryUrl);

    return new DefaultKafkaProducerFactory<Long, String>((Map) config);
  }

  @Bean
  public KafkaTemplate<Long, String> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

}
