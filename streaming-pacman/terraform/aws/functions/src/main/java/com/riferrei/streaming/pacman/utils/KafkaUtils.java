package com.riferrei.streaming.pacman.utils;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import static com.riferrei.streaming.pacman.utils.Constants.*;

public class KafkaUtils {

    public static void createTopic(String topic, int numPartitions,
        short replicationFactor, Map<String, String> configs) {
        try (AdminClient adminClient = AdminClient.create(getConnectProperties())) {
            ListTopicsResult listTopics = adminClient.listTopics();
            Set<String> existingTopics = listTopics.names().get();
            List<NewTopic> topicsToCreate = new ArrayList<>();
            if (!existingTopics.contains(topic)) {
                NewTopic newTopic = new NewTopic(topic, numPartitions, replicationFactor);
                newTopic = newTopic.configs(configs);
                topicsToCreate.add(newTopic);
            }
            adminClient.createTopics(topicsToCreate);
        } catch (InterruptedException | ExecutionException ex) {}
    }

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

    public static KafkaProducer<String, String> createProducer() {
        Properties properties = getConnectProperties();
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
        return producer;
    }

    private static Properties getConnectProperties() {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", BOOTSTRAP_SERVERS);
        properties.setProperty("ssl.endpoint.identification.algorithm", "https");
        properties.setProperty("sasl.mechanism", "PLAIN");
        properties.setProperty("security.protocol", "SASL_SSL");
        properties.setProperty("sasl.jaas.config", getJaaSConfig());
        return properties;
    }

    private static String getJaaSConfig() {
        final StringBuilder jaasConfig = new StringBuilder();
        jaasConfig.append("org.apache.kafka.common.security.plain.PlainLoginModule ");
        jaasConfig.append("required username=\"").append(CLUSTER_API_KEY).append("\" ");
        jaasConfig.append("password=\"").append(CLUSTER_API_SECRET).append("\"; ");
        return jaasConfig.toString();
    }

}
