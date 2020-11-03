package io.confluent.developer.ccloud.demo.kstream.topic;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("topics.transaction-success")
public class TransactionSuccessTopicConfig extends TopicConfig {

}
