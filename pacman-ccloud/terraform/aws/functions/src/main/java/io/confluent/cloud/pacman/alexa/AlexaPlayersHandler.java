package io.confluent.cloud.pacman.alexa;

import java.util.ArrayList;
import java.util.Collections;
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

        if (cacheServer.dbSize() == 0) {
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
            int numberOfPlayers = 1;
            try {
                numberOfPlayers = Integer.parseInt(slot.getValue());
            } catch (NumberFormatException nfe) {}
            speechText = getTopNPlayers(numberOfPlayers);
        }

        return input.getResponseBuilder()
            .withSpeech(speechText)
            .build();

    }

    private String getBestPlayer() {
        
        final StringBuilder speechText = new StringBuilder();

        int playersAvailable = cacheServer.dbSize().intValue();
        List<Player> players = new ArrayList<>(playersAvailable);
        Set<String> keys = cacheServer.keys("*");
        String value = null;
        for (String key : keys) {
            value = cacheServer.get(key);
            players.add(Player.getPlayer(value));
        }
        Collections.sort(players);

        Player userData = players.get(0);
        speechText.append("The best player is ");
        speechText.append(userData.getUser());
        speechText.append(".");

        return speechText.toString();

    }

    private String getTopNPlayers(int numberOfPlayers) {

        final StringBuilder speechText = new StringBuilder();

        int playersAvailable = cacheServer.dbSize().intValue();
        List<Player> players = new ArrayList<>(playersAvailable);
        Set<String> keys = cacheServer.keys("*");
        String value = null;
        for (String key : keys) {
            value = cacheServer.get(key);
            players.add(Player.getPlayer(value));
        }
        Collections.sort(players);

        if (numberOfPlayers == 1) {
            return getBestPlayer();
        } else {
            if (playersAvailable >= numberOfPlayers) {
                speechText.append("The top ");
                speechText.append(numberOfPlayers);
                speechText.append(" players are ");
                if (numberOfPlayers == 2) {
                    Player firstWinner = players.get(0);
                    Player secondWinner = players.get(1);
                    speechText.append(firstWinner.getUser());
                    speechText.append(" and ");
                    speechText.append(secondWinner.getUser());
                } else {
                    for (int i = 0; i < numberOfPlayers; i++) {
                        Player userData = players.get(i);
                        if ((i + 1) == numberOfPlayers) {
                            speechText.append("and ");
                            speechText.append(userData.getUser());
                            speechText.append(".");
                        } else {
                            speechText.append(userData.getUser());
                            speechText.append(", ");
                        }
                    }
                }
            } else {
                speechText.append("I can't tell you who are ");
                speechText.append("the top ").append(numberOfPlayers);
                speechText.append(" because currently there ");
                if (playersAvailable == 1) {
                    speechText.append("is only one player available.");
                } else {
                    speechText.append("are only ").append(playersAvailable);
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
