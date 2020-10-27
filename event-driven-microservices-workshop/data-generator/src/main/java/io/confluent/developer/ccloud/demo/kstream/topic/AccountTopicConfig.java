package io.confluent.developer.ccloud.demo.kstream.topic;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("topics.account")
public class AccountTopicConfig extends TopicConfig {

}
