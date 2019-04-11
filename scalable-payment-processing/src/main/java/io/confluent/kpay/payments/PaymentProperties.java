package io.confluent.kpay.payments;

import io.confluent.kpay.payments.model.Payment;
import io.confluent.kpay.util.KafkaTopicClient;
import io.confluent.kpay.util.PropertiesGetter;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

public class PaymentProperties {
    final static int partitionCount = 5;
    final static short replicaCount = 1;

    final static public String paymentsIncomingTopic = "kpay.payments.incoming";
    final static public String paymentsInflightTopic = "kpay.payments.inflight";
    final static public String paymentsCompleteTopic = "kpay.payments.complete";
    final static public String paymentsConfirmedTopic = "kpay.payments.confirmed";


    public static Properties get(String broker, int portOffset) {
        Properties properties = PropertiesGetter.getProperties(broker, Serdes.String().getClass().getName(), Payment.Serde.class.getName());
        // payment processors startProcessors from 20000
        properties.put(StreamsConfig.APPLICATION_SERVER_CONFIG, PropertiesGetter.getIpAddress() + ":" + portOffset);
        System.out.println(" APP PORT:" + properties.get(StreamsConfig.APPLICATION_SERVER_CONFIG));
        return properties;
    }

    public static void initializeEnvironment(KafkaTopicClient topicClient) {
        topicClient.createTopic(PaymentProperties.paymentsInflightTopic, partitionCount, replicaCount);
        topicClient.createTopic(PaymentProperties.paymentsIncomingTopic, partitionCount, replicaCount);
        topicClient.createTopic(PaymentProperties.paymentsCompleteTopic, partitionCount, replicaCount);
        topicClient.createTopic(PaymentProperties.paymentsConfirmedTopic, partitionCount, replicaCount);

    }
}
