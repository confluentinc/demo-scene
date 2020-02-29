package io.confluent.cloud.pacman.alexa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
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

public class AlexaDetailsHandler implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return input.matches(intentName(PLAYER_DETAILS_INTENT));
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

        String slotName = null;
        String slotValue = null;
        Map<String, Slot> slots = intentRequest.getIntent().getSlots();
        Set<String> slotKeys = slots.keySet();
        for (String slotKey : slotKeys) {
            Slot slot = slots.get(slotKey);
            if (slot.getValue() != null && slot.getValue().length() > 0) {
                slotName = slot.getName();
                slotValue = slot.getValue();
            }
        }

        if (slotName == null && slotValue == null) {
            speechText = "Sorry, but I didn't understand your question.";
        }

        if (slotName.equals(POSITION_RELATIVE_SLOT) ||
            slotName.equals(POSITION_ABSOLUTE_SLOT)) {
            try {
                int position = Integer.parseInt(slotValue);
                speechText = getPlayerDetailsByPosition(--position);
            } catch (NumberFormatException nfe) {}
        } else if (slotName.equals(PLAYER_NAME_SLOT)) {
            speechText = getPlayerDetailsByName(slotValue);
        }

        return input.getResponseBuilder()
            .withSpeech(speechText)
            .build();

    }

    private String getPlayerDetailsByPosition(int position) {
        
        final StringBuilder speechText = new StringBuilder();

        int playersAvailable = cacheServer.dbSize().intValue();
        if (position < playersAvailable) {
            List<Player> players = new ArrayList<>(playersAvailable);
            Set<String> keys = cacheServer.keys("*");
            String value = null;
            for (String key : keys) {
                value = cacheServer.get(key);
                players.add(Player.getPlayer(value));
            }
            Collections.sort(players);
            Player player = players.get(position);
            speechText.append(getPlayerDetails(player));
        } else {
            speechText.append("Sorry but I couldn't find ");
            speechText.append("anyone under this position.");
        }

        return speechText.toString();

    }

    private String getPlayerDetailsByName(String playerName) {

        final StringBuilder speechText = new StringBuilder();

        String value = cacheServer.get(playerName);
        if (value != null) {
            Player player = Player.getPlayer(value);
            speechText.append(getPlayerDetails(player));
        } else {
            speechText.append("Sorry but I couldn't find ");
            speechText.append("anyone with the name ");
            speechText.append(playerName);
        }

        return speechText.toString();

    }

    private String getPlayerDetails(Player player) {

        final StringBuilder speechText = new StringBuilder();
        speechText.append("Here is the latest about '");
        speechText.append(player.getUser()).append("': ");
        speechText.append("current score is ");
        speechText.append(player.getScore());
        speechText.append(" while playing on level ");
        speechText.append(player.getLevel());
        
        switch (player.getLosses()) {
            case 0:
                speechText.append(". Interestingly, ").append(player.getUser());
                speechText.append("didn't die not even once");
                speechText.append(maybeSaySomethingFunny());
                break;
            case 1:
                speechText.append(". Also, ").append(player.getUser());
                speechText.append(" died just once.");
                break;
            default:
                speechText.append(player.getUser());
                speechText.append(" had ").append(player.getLosses());
                speechText.append(" so far.");
                break;
        }
        
        return speechText.toString();
    }

    private String maybeSaySomethingFunny() {
        if (RANDOM.nextInt(10) > 7) {
            int index = RANDOM.nextInt(FUNNY_COMMENTS_DIE.length - 1);
            return FUNNY_COMMENTS_DIE[index];
        }
        return "";
    }

    private static final Random RANDOM = new Random();
    private static final String[] FUNNY_COMMENTS_DIE = {
        ". That is what I would call <break time=\"10ms\"/> 'Die Hard'.",
        ". I guess we have a HighLander in the room, <break time=\"500ms\"/> right?",
        ". I supposed these ghosts should try a little harder."
    };

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
