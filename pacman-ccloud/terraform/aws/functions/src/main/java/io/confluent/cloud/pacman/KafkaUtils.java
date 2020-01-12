package io.confluent.cloud.pacman;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;

import static io.confluent.cloud.pacman.Constants.*;

public class KafkaUtils {

    public static void createTopics(Map<String, Integer> topics) {
        try (AdminClient adminClient = AdminClient.create(getConnectProperties())) {
            ListTopicsResult listTopics = adminClient.listTopics();
            Set<String> existingTopics = listTopics.names().get();
            List<NewTopic> topicsToCreate = new ArrayList<>();
            for (String topic : topics.keySet()) {
                if (!existingTopics.contains(topic)) {
                    topicsToCreate.add(new NewTopic(topic,
                        topics.get(topic), (short) 3));
                }
            }
            adminClient.createTopics(topicsToCreate);
        } catch (InterruptedException | ExecutionException ex) {}
    }

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