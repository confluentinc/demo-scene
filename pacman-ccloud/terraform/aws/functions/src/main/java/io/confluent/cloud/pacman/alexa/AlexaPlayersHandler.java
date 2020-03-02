package io.confluent.cloud.pacman.alexa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;

import io.confluent.cloud.pacman.utils.Player;
import redis.clients.jedis.Jedis;

import static com.amazon.ask.request.Predicates.intentName;
import static io.confluent.cloud.pacman.utils.Constants.*;

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

        if (cacheServer.zcard(SCOREBOARD_CACHE) == 0) {
            speechText = "Sorry but there are no players";
            return input.getResponseBuilder()
                .withSpeech(speechText)
                .build();
        }
        
        if (input.matches(intentName(BEST_PLAYER_INTENT))) {
            speechText = getBestPlayer();
        } else if (input.matches(intentName(TOPN_PLAYERS_INTENT))) {
            Map<String, Slot> slots = intentRequest.getIntent().getSlots();
            Slot slot = slots.get(NUMBER_OF_PLAYERS_SLOT);
            int topNPlayers = 0;
            try {
                topNPlayers = Integer.parseInt(slot.getValue());
            } catch (NumberFormatException nfe) {}
            speechText = getTopNPlayers(topNPlayers);
        }

        return input.getResponseBuilder()
            .withSpeech(speechText)
            .build();

    }

    private String getBestPlayer() {
        
        final StringBuilder speechText = new StringBuilder();

        Set<String> bestPlayerKey = cacheServer.zrevrange(SCOREBOARD_CACHE, 0, 0);
        String key = bestPlayerKey.iterator().next();
        String value = cacheServer.get(key);
        Player player = Player.getPlayer(value);
        speechText.append("The best player is ");
        speechText.append(player.getUser());
        speechText.append(".");

        return speechText.toString();

    }

    private String getTopNPlayers(int topNPlayers) {

        final StringBuilder speechText = new StringBuilder();

        if (topNPlayers == 1) {
            return getBestPlayer();
        } else {

            Set<String> playerKeys = cacheServer.zrevrange(SCOREBOARD_CACHE, 0, topNPlayers - 1);
            List<Player> players = new ArrayList<>(playerKeys.size());
            for (String key : playerKeys) {
                String value = cacheServer.get(key);
                players.add(Player.getPlayer(value));
            }

            if (players.size() >= topNPlayers) {
                speechText.append("The top ");
                speechText.append(topNPlayers);
                speechText.append(" players are ");
                if (topNPlayers == 2) {
                    Player firstPlayer = players.get(0);
                    Player secondPlayer = players.get(1);
                    speechText.append(firstPlayer.getUser());
                    speechText.append(" and ");
                    speechText.append(secondPlayer.getUser());
                } else {
                    for (int i = 0; i < topNPlayers; i++) {
                        Player player = players.get(i);
                        if ((i + 1) == topNPlayers) {
                            speechText.append("and ");
                            speechText.append(player.getUser());
                            speechText.append(".");
                        } else {
                            speechText.append(player.getUser());
                            speechText.append(", ");
                        }
                    }
                }
            } else {
                speechText.append("I can't tell you who are ");
                speechText.append("the top ").append(topNPlayers);
                speechText.append(" because currently there ");
                if (players.size() == 1) {
                    speechText.append("is only one player available.");
                } else {
                    speechText.append("are only ").append(players.size());
                    speechText.append(" players available.");
                }
            }

        }

        return speechText.toString();

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
