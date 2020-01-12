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

import static io.confluent.cloud.pacman.Constants.*;
import static io.confluent.cloud.pacman.KafkaUtils.*;

public class EventHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    public Map<String, Object> handleRequest(final Map<String, Object> request, final Context context) {
        
        final Map<String, Object> requestHeaders =
            (Map<String, Object>) request.get(HEADERS_KEY);
        final Map<String, Object> response = new HashMap<>();

        if (!request.containsKey(HEADERS_KEY)) {
            response.put(BODY_KEY, "Thanks for waking me up");
            return response;
        }

        if (requestHeaders.containsKey(ORIGIN_KEY)) {

            String origin = (String) requestHeaders.get(ORIGIN_KEY);
            if (origin.equals(ORIGIN_ALLOWED)) {
                if (request.containsKey(QUERY_PARAMS_KEY) && request.containsKey(BODY_KEY)) {
        
                    String event = (String) request.get(BODY_KEY);
                    Map<String, String> queryParams = (Map<String, String>)
                        request.get(QUERY_PARAMS_KEY);
                    
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

                        final Map<String, Object> responseHeaders = new HashMap<>();
                        responseHeaders.put("Access-Control-Allow-Headers", "*");
                        responseHeaders.put("Access-Control-Allow-Methods", POST_METHOD);
                        responseHeaders.put("Access-Control-Allow-Origin", ORIGIN_ALLOWED);
                        response.put(HEADERS_KEY, responseHeaders);

                    }
        
                }
                
            }
        }
        return response;

    }

    private static final String USER_GAME_TOPIC = "USER_GAME";
    private static final String USER_LOSSES_TOPIC = "USER_LOSSES";
    private static KafkaProducer<String, String> producer;

    static {
        createTopicsIfNeeded();
        initializeProducer();
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

}
