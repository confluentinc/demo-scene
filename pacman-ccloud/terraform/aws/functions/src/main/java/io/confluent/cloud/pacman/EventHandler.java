package io.confluent.cloud.pacman;

import java.util.Collections;
import java.util.HashMap;
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
        logger.log("Request Type: " + request.getClass().getName());
        logger.log("Request Content: " + request);

        Map<String, Object> headers = new HashMap<String, Object>();
        Map<String, Object> response = new HashMap<String, Object>();
        headers.put("Content-Type", "text/plain");
        headers.put("Access-Control-Allow-Origin", "*");
        response.put("headers", headers);
        response.put("statusCode", 200);

        if (request.containsKey(QUERY_PARAMS_KEY) && request.containsKey(BODY_KEY)) {

            createTopicsIfNeeded();
            ensureProducerIsReady();

            String event = (String) request.get(BODY_KEY);
            Map<String, String> queryParams = (Map<String, String>) request.get(QUERY_PARAMS_KEY);
            String topic = queryParams.get(TOPIC_KEY);
            
            producer.send(new ProducerRecord<String, String>(topic, event), new Callback() {
                public void onCompletion(final RecordMetadata metadata, final Exception exception) {
                    StringBuilder message = new StringBuilder();
                    message.append("Event emmited successfully to topic '");
                    message.append(metadata.topic()).append("'.");
                    response.put(BODY_KEY, message.toString());
                }
            });
            producer.flush();

        } else {
            logger.log("Wake up event received");
        }
        
        return response;

    }

    private Properties getConnectProperties() {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", BOOTSTRAP_SERVERS);
        properties.setProperty("ssl.endpoint.identification.algorithm", "https");
        properties.setProperty("sasl.mechanism", "PLAIN");
        properties.setProperty("security.protocol", "SASL_SSL");
        properties.setProperty("sasl.jaas.config", getJaaSConfig());
        return properties;
    }

    private void createTopicsIfNeeded() {
        Properties properties = getConnectProperties();
        try (AdminClient adminClient = AdminClient.create(properties)) {
            ListTopicsResult topics = adminClient.listTopics();
            Set<String> topicNames = topics.names().get();
            if (!topicNames.contains(USER_GAME_TOPIC)) {
                NewTopic newTopic = new NewTopic(USER_GAME_TOPIC, 6, (short) 3);
                adminClient.createTopics(Collections.singletonList(newTopic));
            }
            if (!topicNames.contains(USER_LOSSES_TOPIC)) {
                NewTopic newTopic = new NewTopic(USER_LOSSES_TOPIC, 6, (short) 3);
                adminClient.createTopics(Collections.singletonList(newTopic));
            }
        } catch (InterruptedException | ExecutionException ex) {}
    }

    private void ensureProducerIsReady() {
        if (producer == null) {
            Properties properties = getConnectProperties();
            properties.setProperty(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, "500");
            properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
            producer = new KafkaProducer<String, String>(properties);
        }
    }

    private String getJaaSConfig() {
        final StringBuilder jaasConfig = new StringBuilder();
        jaasConfig.append("org.apache.kafka.common.security.plain.PlainLoginModule ");
        jaasConfig.append("required username=\"").append(CLUSTER_API_KEY).append("\" ");
        jaasConfig.append("password=\"").append(CLUSTER_API_SECRET).append("\"; ");
        return jaasConfig.toString();
    }

    private static final String TOPIC_KEY = "topic";
    private static final String BODY_KEY = "body";
    private static final String QUERY_PARAMS_KEY = "queryStringParameters";
    private static final String USER_GAME_TOPIC = "USER_GAME";
    private static final String USER_LOSSES_TOPIC = "USER_LOSSES";
    private static final String BOOTSTRAP_SERVERS = System.getenv("BOOTSTRAP_SERVERS");
    private static final String CLUSTER_API_KEY = System.getenv("CLUSTER_API_KEY");
    private static final String CLUSTER_API_SECRET = System.getenv("CLUSTER_API_SECRET");
    private static KafkaProducer<String, String> producer;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                producer.close();
            }
        });
    }

}
