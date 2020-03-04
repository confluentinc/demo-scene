package io.confluent.cloud.pacman;

import java.util.Map;
import java.util.HashMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import static io.confluent.cloud.pacman.utils.Constants.*;
import static io.confluent.cloud.pacman.utils.KafkaUtils.*;

public class EventHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    public Map<String, Object> handleRequest(final Map<String, Object> request, final Context context) {
        
        Map<String, Object> response = new HashMap<>();
        if (!request.containsKey(HEADERS_KEY)) {
            response.put(BODY_KEY, "Thanks for waking me up");
            return response;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> requestHeaders =
            (Map<String, Object>) request.get(HEADERS_KEY);

        if (requestHeaders.containsKey(ORIGIN_KEY)) {

            String origin = (String) requestHeaders.get(ORIGIN_KEY);

            if (origin.equals(ORIGIN_ALLOWED)) {

                if (request.containsKey(QUERY_PARAMS_KEY) && request.containsKey(BODY_KEY)) {

                    String event = (String) request.get(BODY_KEY);
                    @SuppressWarnings("unchecked")
                    Map<String, String> queryParams =
                        (Map<String, String>) request.get(QUERY_PARAMS_KEY);

                    if (event != null && queryParams != null) {

                        String topic = queryParams.get(TOPIC_KEY);
                        String user = extractUserFromEvent(event);

                        ProducerRecord<String, String> record = new ProducerRecord<>(topic, user, event);
                        producer.send(record, (metadata, exception) -> {
                            StringBuilder message = new StringBuilder();
                            message.append("Event sent successfully to topic '");
                            message.append(metadata.topic()).append("' on the ");
                            message.append("partition ").append(metadata.partition());
                            response.put(BODY_KEY, message.toString());
                        });
                        producer.flush();

                        Map<String, Object> responseHeaders = new HashMap<>();
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

    private String extractUserFromEvent(String payload) {
        JsonElement root = JsonParser.parseString(payload);
        return root.getAsJsonObject().get("user").getAsString();
    }

    private static KafkaProducer<String, String> producer;

    static {
        producer = createProducer();
        createTopics(Map.of(USER_GAME_TOPIC, 6,
                            USER_LOSSES_TOPIC, 6));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (producer != null) {
                producer.close();
            }
        }));
    }

}
