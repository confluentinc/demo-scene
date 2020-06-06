package com.riferrei.streaming.pacman.alexa;

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

public class AlexaDetailsHandler implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return input.matches(intentName(PLAYER_DETAILS_INTENT));
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
            speechText = resourceBundle.getString(FAILED_QUESTION);
        }

        if (slotName.equals(POSITION_RELATIVE_SLOT) ||
            slotName.equals(POSITION_ABSOLUTE_SLOT)) {
            try {
                int position = Integer.parseInt(slotValue);
                speechText = getPlayerDetailsByPosition(position, resourceBundle);
            } catch (NumberFormatException nfe) {}
        } else if (slotName.equals(PLAYER_NAME_SLOT)) {
            speechText = getPlayerDetailsByName(slotValue, resourceBundle);
        }

        return input.getResponseBuilder()
            .withSpeech(speechText)
            .build();

    }

    private String getPlayerDetailsByPosition(int position, ResourceBundle resourceBundle) {
        
        final StringBuilder speechText = new StringBuilder();

        if (position <= cacheServer.zcard(SCOREBOARD_CACHE)) {
            position = position - 1;
            Set<String> playerKey = cacheServer.zrevrange(SCOREBOARD_CACHE, position, position);
            String key = playerKey.iterator().next();
            String value = cacheServer.get(key);
            Player player = Player.getPlayer(value);
            speechText.append(getPlayerDetails(player, resourceBundle));
        } else {
            speechText.append(resourceBundle.getString(POSITION_DOESNT_EXIST));
        }

        return speechText.toString();

    }

    private String getPlayerDetailsByName(String playerName, ResourceBundle resourceBundle) {

        final StringBuilder speechText = new StringBuilder();

        if (cacheServer.exists(playerName)) {
            String value = cacheServer.get(playerName);
            Player player = Player.getPlayer(value);
            speechText.append(getPlayerDetails(player, resourceBundle));
        } else {
            String text = resourceBundle.getString(NO_ONE_WITH_THIS_NAME);
            speechText.append(String.format(text, playerName));
        }

        return speechText.toString();

    }

    private String getPlayerDetails(Player player, ResourceBundle resourceBundle) {

        final StringBuilder speechText = new StringBuilder();

        String text = resourceBundle.getString(PLAYER_DETAILS);
        speechText.append(String.format(text, player.getUser(),
            player.getScore(), player.getLevel()));
        
        switch (player.getLosses()) {
            case 0:
                text = resourceBundle.getString(ZERO_LOSSES_DETAILS);
                speechText.append(String.format(text, player.getUser()));
                break;
            case 1:
                text = resourceBundle.getString(ONE_LOSS_DETAILS);
                speechText.append(String.format(text, player.getUser()));
                break;
            default:
                text = resourceBundle.getString(N_LOSSES_DETAILS);
                speechText.append(String.format(text, player.getUser(), player.getLosses()));
                break;
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
