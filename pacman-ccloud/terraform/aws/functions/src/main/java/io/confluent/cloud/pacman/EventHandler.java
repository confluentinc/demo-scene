package io.confluent.cloud.pacman;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

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

            final String origin = (String) requestHeaders.get(ORIGIN_KEY);
            if (origin.equals(ORIGIN_ALLOWED)) {
                if (request.containsKey(QUERY_PARAMS_KEY) && request.containsKey(BODY_KEY)) {
        
                    final String event = (String) request.get(BODY_KEY);
                    final Map<String, String> queryParams =
                        (Map<String, String>) request.get(QUERY_PARAMS_KEY);
                    
                    if (event != null && queryParams != null) {

                        final String topic = queryParams.get(TOPIC_KEY);
                        producer.send(new ProducerRecord<String, String>(topic, event), new Callback() {
                            public void onCompletion(final RecordMetadata metadata, final Exception ex) {
                                final StringBuilder message = new StringBuilder();
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
        createTopics(Map.of(USER_GAME_TOPIC, 6,
                            USER_LOSSES_TOPIC, 6));
        initializeProducer();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (producer != null) {
                producer.close();
            }
        }));
    }

    private static void initializeProducer() {
        if (producer == null) {
            final Properties properties = getConnectProperties();
            properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            producer = new KafkaProducer<>(properties);
        }
    }

}
