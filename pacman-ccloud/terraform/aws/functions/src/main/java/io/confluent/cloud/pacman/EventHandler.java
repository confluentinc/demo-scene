package io.confluent.cloud.pacman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;

public class EventHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    public Map<String, Object> handleRequest(final Map<String, Object> request, final Context context) {

        final LambdaLogger logger = context.getLogger();
        final Map<String, Object> response = new HashMap<>();
        logger.log("Request Content: " + request);

        if (!request.containsKey(HEADERS_KEY)) {
            response.put(BODY_KEY, "Thanks for waking me up");
            return response;
        }

        final Map<String, Object> requestHeaders =
            (Map<String, Object>) request.get(HEADERS_KEY);

        if (requestHeaders.containsKey(ORIGIN_KEY)) {
            String origin = (String) requestHeaders.get(ORIGIN_KEY);
            if (origin.equals(ORIGIN_ALLOWED)) {
                if (request.containsKey(QUERY_PARAMS_KEY) && request.containsKey(BODY_KEY)) {
        
                    String event = (String) request.get(BODY_KEY);
                    Map<String, String> queryParams =
                        (Map<String, String>) request.get(QUERY_PARAMS_KEY);
                    
                    if (event != null && queryParams != null) {
                        String topic = queryParams.get(TOPIC_KEY);
                        producer.send(new ProducerRecord<String, String>(topic, event), new Callback() {
                            public void onCompletion(final RecordMetadata metadata, final Exception ex) {
                                StringBuilder message = new StringBuilder();
                                message.append("Event sent successfully to topic '");
                                message.append(metadata.topic()).append("' on the ");
                                message.append("partition ").append(metadata.partition());
                                response.put(BODY_KEY, message.toString());
                            }
                        });
                        producer.flush();
                    }
        
                }
                
            }
        }

        return response;

    }

    private static final String TOPIC_KEY = "topic";
    private static final String BODY_KEY = "body";
    private static final String ORIGIN_KEY = "origin";
    private static final String HEADERS_KEY = "headers";
    private static final String QUERY_PARAMS_KEY = "queryStringParameters";
    private static final String USER_GAME_TOPIC = "USER_GAME";
    private static final String USER_LOSSES_TOPIC = "USER_LOSSES";
    private static final String BOOTSTRAP_SERVERS = System.getenv("BOOTSTRAP_SERVERS");
    private static final String CLUSTER_API_KEY = System.getenv("CLUSTER_API_KEY");
    private static final String CLUSTER_API_SECRET = System.getenv("CLUSTER_API_SECRET");
    private static final String ORIGIN_ALLOWED = System.getenv("ORIGIN_ALLOWED");
    private static KafkaProducer<String, String> producer;

    static {
        initializeProducer();
        createTopicsIfNeeded();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (producer != null) {
                producer.close();
            }
        }));
    }

    private static void initializeProducer() {
        if (producer == null) {
            Properties properties = getConnectProperties();
            properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            producer = new KafkaProducer<>(properties);
        }
    }

    private static void createTopicsIfNeeded() {
        try (AdminClient adminClient = AdminClient.create(getConnectProperties())) {
            ListTopicsResult listTopics = adminClient.listTopics();
            Set<String> existingTopics = listTopics.names().get();
            List<NewTopic> topicsToCreate = new ArrayList<>();
            if (!existingTopics.contains(USER_GAME_TOPIC)) {
                topicsToCreate.add(new NewTopic(USER_GAME_TOPIC, 6, (short) 3));
            }
            if (!existingTopics.contains(USER_LOSSES_TOPIC)) {
                topicsToCreate.add(new NewTopic(USER_LOSSES_TOPIC, 6, (short) 3));
            }
            adminClient.createTopics(topicsToCreate);
        } catch (InterruptedException | ExecutionException ex) {}
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
