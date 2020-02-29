package io.confluent.cloud.pacman;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
        Set<String> playerIds = null;

        if (player != null) {

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

            playerIds = cacheServer.keys("*");
            int numberOfPlayers = cacheServer.dbSize().intValue();
            JsonArray entries = new JsonArray(numberOfPlayers);
            List<Player> players = new ArrayList<>(numberOfPlayers);
    
            String value = null;
            for (String key : playerIds) {
                value = cacheServer.get(key);
                players.add(Player.getPlayer(value));
            }
            Collections.sort(players);
    
            for (Player _player : players) {
                JsonObject playerEntry = new JsonObject();
                playerEntry.addProperty(Player.USER, _player.getUser());
                playerEntry.addProperty(Player.SCORE, _player.getScore());
                playerEntry.addProperty(Player.LEVEL, _player.getLevel());
                playerEntry.addProperty(Player.LOSSES, _player.getLosses());
                entries.add(playerEntry);
            }
    
            rootObject.add(SCOREBOARD_FIELD, entries);

        }

        return new Gson().toJson(rootObject);

    }

    private static final String CACHE_SERVER_HOST = System.getenv("CACHE_SERVER_HOST");
    private static final String CACHE_SERVER_PORT = System.getenv("CACHE_SERVER_PORT");
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
