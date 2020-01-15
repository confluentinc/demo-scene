package io.confluent.cloud.pacman;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import static io.confluent.cloud.pacman.Constants.*;
import static io.confluent.cloud.pacman.KafkaUtils.*;

public class Scoreboard implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final ConcurrentHashMap<String, UserData> scoreboard = new ConcurrentHashMap<>();

    public Map<String, Object> handleRequest(Map<String, Object> request, Context context) {

        updateScoreboard(100);
        Map<String, Object> response = new HashMap<>();
        response.put(BODY_KEY, getScoreboard());

        Map<String, Object> responseHeaders = new HashMap<>();
        responseHeaders.put("Access-Control-Allow-Headers", "*");
        responseHeaders.put("Access-Control-Allow-Methods", POST_METHOD);
        responseHeaders.put("Access-Control-Allow-Origin", ORIGIN_ALLOWED);
        response.put(HEADERS_KEY, responseHeaders);

        return response;

    }

    private static String getScoreboard() {

        JsonArray entries = new JsonArray(scoreboard.size());
        Enumeration<UserData> _users = scoreboard.elements();
        List<UserData> users = Collections.list(_users);
        Collections.sort(users);
        for (UserData user : users) {
            JsonObject userEntry = new JsonObject();
            userEntry.addProperty(UserData.USER, user.getUser());
            userEntry.addProperty(UserData.SCORE, user.getScore());
            userEntry.addProperty(UserData.LEVEL, user.getLevel());
            userEntry.addProperty(UserData.LOSSES, user.getLosses());
            entries.add(userEntry);
        }

        JsonObject rootObject = new JsonObject();
        rootObject.add(SCOREBOARD_FIELD, entries);
        return new Gson().toJson(rootObject);

    }

    private static void updateScoreboard(long timeout) {
        ConsumerRecords<String, String> records =
            consumer.poll(Duration.ofMillis(timeout));
        try {
            for (ConsumerRecord<String, String> record : records) {
                UserData userData = getUserData(record.value());
                if (userData != null) {
                    scoreboard.put(userData.getUser(), userData);
                }
            }
        } finally {
            consumer.commitAsync();
        }
    }

    private static UserData getUserData(String payload) {
        JsonElement rootElement = JsonParser.parseString(payload);
        JsonObject rootObject = rootElement.getAsJsonObject();
        JsonElement element = null;
        String user = null;
        if (rootObject.has(USER_FIELD)) {
            element = rootObject.get(USER_FIELD);
            if (element instanceof JsonNull == false) {
                user = element.getAsString();
            } else {
                return null;
            }
        }
        int score = 0;
        if (rootObject.has(HIGHEST_SCORE_FIELD)) {
            element = rootObject.get(HIGHEST_SCORE_FIELD);
            if (element instanceof JsonNull == false) {
                score = element.getAsInt();
            }
        }
        int level = 0;
        if (rootObject.has(HIGHEST_LEVEL_FIELD)) {
            element = rootObject.get(HIGHEST_LEVEL_FIELD);
            if (element instanceof JsonNull == false) {
                level = element.getAsInt();
            }
        }
        int losses = 0;
        if (rootObject.has(TOTAL_LOSSES_FIELD)) {
            element = rootObject.get(TOTAL_LOSSES_FIELD);
            if (element instanceof JsonNull == false) {
                losses = element.getAsInt();
            }
        }
        return new UserData(user, score, level, losses);
    }

    private static final String SCOREBOARD_FIELD = "scoreboard";
    private static final String SCOREBOARD_TOPIC = "SCOREBOARD";
    private static final String HIGHEST_SCORE_FIELD = "HIGHEST_SCORE";
    private static final String HIGHEST_LEVEL_FIELD = "HIGHEST_LEVEL";
    private static final String TOTAL_LOSSES_FIELD = "TOTAL_LOSSES";
    private static final String USER_FIELD = "USER";
    private static KafkaConsumer<String, String> consumer;

    static {
        Map<String, String> configs = new HashMap<>();
        configs.put("cleanup.policy", "compact");
        configs.put("delete.retention.ms", "100");
        createTopic(SCOREBOARD_TOPIC, 6, (short) 3, configs);
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
            updateScoreboard(60000);
        }
    }

    private static class UserData implements Comparable<UserData> {

        private static final String USER = "user";
        private static final String SCORE = "score";
        private static final String LEVEL = "level";
        private static final String LOSSES = "losses";

        private String user;
        private int score;
        private int level;
        private int losses;

        public UserData(
            String user,
            int score,
            int level,
            int losses) {
            this.user = user;
            this.score = score;
            this.level = level;
            this.losses = losses;
        }

        public String getUser() {
            return user;
        }
    
        public int getScore() {
            return score;
        }
    
        public int getLevel() {
            return level;
        }

        public int getLosses() {
            return losses;
        }

        public int compareTo(UserData other) {
            if (this.getScore() > other.getScore()) {
                return -1;
            } else if (this.getScore() < other.getScore()) {
                return 1;
            } else {
                if (this.getLevel() > other.getLevel()) {
                    return -1;
                } else if (this.getLevel() < other.getLevel()) {
                    return 1;
                }
            }
            if (this.getLosses() < other.getLosses()) {
                return -1;
            }
            return 0;
        }
    
    }

}
