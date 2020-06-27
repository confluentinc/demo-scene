package com.riferrei.streaming.pacman.alexa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;

import com.riferrei.streaming.pacman.utils.Player;
import redis.clients.jedis.Jedis;

import static com.amazon.ask.request.Predicates.intentName;
import static com.riferrei.streaming.pacman.utils.Constants.*;
import static com.riferrei.streaming.pacman.utils.SkillUtils.*;

public class AlexaPlayersHandler implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return input.matches(intentName(BEST_PLAYER_INTENT)
            .or(intentName(TOPN_PLAYERS_INTENT)));
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {

        cacheServer.connect();
        String speechText = null;
        ResourceBundle resourceBundle =
            getResourceBundle(input);

        if (cacheServer.zcard(SCOREBOARD_CACHE) == 0) {
            speechText = resourceBundle.getString(NO_PLAYERS);
            return input.getResponseBuilder()
                .withSpeech(speechText)
                .build();
        }
        
        if (input.matches(intentName(BEST_PLAYER_INTENT))) {
            speechText = getBestPlayer(resourceBundle);
        } else if (input.matches(intentName(TOPN_PLAYERS_INTENT))) {
            Map<String, Slot> slots = intentRequest.getIntent().getSlots();
            Slot slot = slots.get(NUMBER_OF_PLAYERS_SLOT);
            int topNPlayers = 1;
            try {
                topNPlayers = Integer.parseInt(slot.getValue());
            } catch (NumberFormatException nfe) {}
            speechText = getTopNPlayers(topNPlayers, resourceBundle);
        }

        return input.getResponseBuilder()
            .withSpeech(speechText)
            .build();

    }

    private String getBestPlayer(ResourceBundle resourceBundle) {
        
        final StringBuilder speechText = new StringBuilder();

        Set<String> bestPlayerKey = cacheServer.zrevrange(SCOREBOARD_CACHE, 0, 0);
        String key = bestPlayerKey.iterator().next();
        String value = cacheServer.get(key);
        Player player = Player.getPlayer(key, value);
        String text = resourceBundle.getString(BEST_PLAYER);
        speechText.append(String.format(text, player.getUser()));

        return speechText.toString();

    }

    private String getTopNPlayers(int topNPlayers, ResourceBundle resourceBundle) {

        final StringBuilder speechText = new StringBuilder();
        long playersAvailable = cacheServer.zcard(SCOREBOARD_CACHE);

        if (playersAvailable >= topNPlayers) {

            Set<String> playerKeys = cacheServer.zrevrange(
                SCOREBOARD_CACHE, 0, topNPlayers - 1);
            List<Player> players = new ArrayList<>(playerKeys.size());
            for (String key : playerKeys) {
                String value = cacheServer.get(key);
                players.add(Player.getPlayer(key, value));
            }

            String and = resourceBundle.getString(AND);
            if (topNPlayers == 1) {
                Player player = players.get(0);
                String text = resourceBundle.getString(TOP_1_PLAYER);
                speechText.append(String.format(text, player.getUser()));
            } else if (topNPlayers == 2) {
                Player firstPlayer = players.get(0);
                Player secondPlayer = players.get(1);
                String text = resourceBundle.getString(TOP_N_PLAYERS);
                speechText.append(String.format(text, topNPlayers));
                speechText.append(firstPlayer.getUser());
                speechText.append(" ").append(and).append(" ");
                speechText.append(secondPlayer.getUser());
            } else {
                String text = resourceBundle.getString(TOP_N_PLAYERS);
                speechText.append(String.format(text, topNPlayers));
                for (int i = 0; i < topNPlayers; i++) {
                    Player player = players.get(i);
                    if ((i + 1) == topNPlayers) {
                        speechText.append(and).append(" ");
                        speechText.append(player.getUser());
                        speechText.append(".");
                    } else {
                        speechText.append(player.getUser());
                        speechText.append(", ");
                    }
                }
            }

        } else {
            String text = resourceBundle.getString(NOT_ENOUGH_PLAYERS);
            speechText.append(String.format(text, topNPlayers, playersAvailable));
        }

        return speechText.toString();

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
