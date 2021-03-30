package com.gnatali.streaming.pacman.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static com.gnatali.streaming.pacman.utils.Constants.*;

public class Player implements Comparable<Player> {

    private String user;
    private int score;
    private int level;
    private int losses;

    public Player(
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

    public int compareTo(Player other) {
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

    public static final String USER = "user";
    public static final String SCORE = "score";
    public static final String LEVEL = "level";
    public static final String LOSSES = "losses";

    public static Player getPlayer(String key, String value) {
        JsonElement rootElement = JsonParser.parseString(value);
        JsonObject rootObject = rootElement.getAsJsonObject();
        JsonElement element = null;
        String user = key;
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
        return new Player(user, score, level, losses);
    }

}
