package io.confluent.kpay.control;

import io.confluent.kpay.control.model.Status;
import io.confluent.kpay.util.KafkaTopicClient;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;

import static io.confluent.kpay.util.PropertiesGetter.getProperties;

public class ControlProperties {
    final public static String controlStatusTopic = "kpay.control.status";
    final static int partitionCount = 5;
    final static short replicaCount = 1;


    public static Properties get(String broker) {
        return getProperties(broker, Serdes.String().getClass().getName(), Status.Serde.class.getName());
    }

    public static void initializeEnvironment(KafkaTopicClient topicClient) {
        topicClient.createTopic(controlStatusTopic, partitionCount, replicaCount);
    }
}
