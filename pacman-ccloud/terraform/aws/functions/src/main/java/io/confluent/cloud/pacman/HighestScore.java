package io.confluent.cloud.pacman;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;
import java.util.Arrays;
import java.util.Properties;
import java.time.Duration;
import java.util.HashMap;
import java.util.UUID;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import static io.confluent.cloud.pacman.Constants.*;
import static io.confluent.cloud.pacman.KafkaUtils.*;

public class HighestScore implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static int highestScore = 0;

    public Map<String, Object> handleRequest(final Map<String, Object> request, final Context context) {

        fetchCurrentHighestScore(100);
        JsonObject jsonResp = new JsonObject();
        jsonResp.addProperty("highestScore", highestScore);
        Map<String, Object> response = new HashMap<>();
        response.put(BODY_KEY, new Gson().toJson(jsonResp));

        final Map<String, Object> responseHeaders = new HashMap<>();
        responseHeaders.put("Access-Control-Allow-Headers", "*");
        responseHeaders.put("Access-Control-Allow-Methods", POST_METHOD);
        responseHeaders.put("Access-Control-Allow-Origin", ORIGIN_ALLOWED);
        response.put(HEADERS_KEY, responseHeaders);

        return response;

    }

    private static void fetchCurrentHighestScore(long timeout) {
        ConsumerRecords<String, String> records = null;
        records = consumer.poll(Duration.ofMillis(timeout));
        for (ConsumerRecord<String, String> record : records) {
            JsonObject root = JsonParser.parseString(record.value()).getAsJsonObject();
            int score = root.get("HIGHEST_SCORE").getAsInt();
            if (score > highestScore) {
                highestScore = score;
            }
        }
        consumer.commitAsync();
    }

    private static final String SCOREBOARD_TOPIC = "SCOREBOARD";
    private static KafkaConsumer<String, String> consumer;

    static {
        createTopics(Map.of(SCOREBOARD_TOPIC, 6));
        initializeConsumer();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (consumer != null) {
                consumer.close();
            }
        }));
    }

    private static void initializeConsumer() {
        if (consumer == null) {
            Properties properties = getConnectProperties();
            properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            consumer = new KafkaConsumer<>(properties);
            consumer.subscribe(Arrays.asList(SCOREBOARD_TOPIC));
            // Wait up to one minute since we're fetching records
            // from the beginning of the log. It may take a while.
            fetchCurrentHighestScore(60000);
        }
    }

}
