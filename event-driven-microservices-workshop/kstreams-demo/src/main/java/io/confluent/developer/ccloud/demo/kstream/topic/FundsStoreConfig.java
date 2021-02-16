package io.confluent.developer.ccloud.demo.kstream.topic;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("stores.funds")
public class FundsStoreConfig {

  private String name = "funds";

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
