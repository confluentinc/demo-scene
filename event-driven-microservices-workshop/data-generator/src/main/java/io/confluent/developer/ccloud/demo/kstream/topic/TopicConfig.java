package io.confluent.developer.ccloud.demo.kstream.topic;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Data;

@Configuration
@PropertySource(value = "classpath:topics-defaults.properties")
@Data
public abstract class TopicConfig {

  private String name;
  private boolean compacted = false;
  private int partitions = 1;
  private short replicationFactor = 1;
  
}
