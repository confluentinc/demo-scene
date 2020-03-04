package io.confluent.cloud.pacman;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import io.confluent.cloud.pacman.utils.Player;
import redis.clients.jedis.Jedis;

import static io.confluent.cloud.pacman.utils.Constants.*;

public class Scoreboard implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    public Map<String, Object> handleRequest(Map<String, Object> request, Context context) {

        final Map<String, Object> response = new HashMap<>();

        String player = null;
        if (request.containsKey(QUERY_PARAMS_KEY)) {
            @SuppressWarnings("unchecked")
            Map<String, String> queryParams =
                (Map<String, String>) request.get(QUERY_PARAMS_KEY);
            if (queryParams != null && queryParams.containsKey(PLAYER_KEY)) {
                player = queryParams.get(PLAYER_KEY);
            }
        }
        
        response.put(BODY_KEY, getScoreboard(player));
        Map<String, Object> responseHeaders = new HashMap<>();
        responseHeaders.put("Access-Control-Allow-Headers", "*");
        responseHeaders.put("Access-Control-Allow-Methods", POST_METHOD);
        responseHeaders.put("Access-Control-Allow-Origin", ORIGIN_ALLOWED);
        response.put(HEADERS_KEY, responseHeaders);

        return response;

    }

    private static String getScoreboard(String player) {

        cacheServer.connect();
        JsonObject rootObject = new JsonObject();

        if (player != null) {

            player = player.toLowerCase();
            JsonObject playerEntry = new JsonObject();

            if (cacheServer.exists(player)) {
                String value = cacheServer.get(player);
                Player _player = Player.getPlayer(value);
                playerEntry.addProperty(Player.USER, _player.getUser());
                playerEntry.addProperty(Player.SCORE, _player.getScore());
                playerEntry.addProperty(Player.LEVEL, _player.getLevel());
                playerEntry.addProperty(Player.LOSSES, _player.getLosses());
            }

            rootObject.add(SCOREBOARD_FIELD, playerEntry);

        } else {

            JsonArray playerEntries = new JsonArray();
            long players = cacheServer.zcard(SCOREBOARD_CACHE);
            if (players > 0) {
                Set<String> playerKeys = cacheServer.zrevrange(SCOREBOARD_CACHE, 0, -1);
                for (String key : playerKeys) {
                    String value = cacheServer.get(key);
                    Player _player = Player.getPlayer(value);
                    JsonObject playerEntry = new JsonObject();
                    playerEntry.addProperty(Player.USER, _player.getUser());
                    playerEntry.addProperty(Player.SCORE, _player.getScore());
                    playerEntry.addProperty(Player.LEVEL, _player.getLevel());
                    playerEntry.addProperty(Player.LOSSES, _player.getLosses());
                    playerEntries.add(playerEntry);
                }
            }
    
            rootObject.add(SCOREBOARD_FIELD, playerEntries);

        }

        return new Gson().toJson(rootObject);

    }

    private static Jedis cacheServer;

    static {
        cacheServer = new Jedis(CACHE_SERVER_HOST, Integer.parseInt(CACHE_SERVER_PORT));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (cacheServer != null) {
                cacheServer.disconnect();
            }
        }));
    }

}
