package io.confluent.cloud.pacman;

import java.util.Properties;

import static io.confluent.cloud.pacman.Constants.*;

public class KafkaUtils {

    public static Properties getConnectProperties() {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", BOOTSTRAP_SERVERS);
        properties.setProperty("ssl.endpoint.identification.algorithm", "https");
        properties.setProperty("sasl.mechanism", "PLAIN");
        properties.setProperty("security.protocol", "SASL_SSL");
        properties.setProperty("sasl.jaas.config", getJaaSConfig());
        return properties;
    }

    public static String getJaaSConfig() {
        final StringBuilder jaasConfig = new StringBuilder();
        jaasConfig.append("org.apache.kafka.common.security.plain.PlainLoginModule ");
        jaasConfig.append("required username=\"").append(CLUSTER_API_KEY).append("\" ");
        jaasConfig.append("password=\"").append(CLUSTER_API_SECRET).append("\"; ");
        return jaasConfig.toString();
    }

}