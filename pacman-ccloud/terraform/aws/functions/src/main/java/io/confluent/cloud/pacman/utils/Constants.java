package io.confluent.cloud.pacman.utils;

public interface Constants {

    public static final String PLAYER_KEY = "player";
    public static final String TOPIC_KEY = "topic";
    public static final String BODY_KEY = "body";
    public static final String ORIGIN_KEY = "origin";
    public static final String HEADERS_KEY = "headers";
    public static final String QUERY_PARAMS_KEY = "queryStringParameters";
    public static final String POST_METHOD = "POST";

    public static final String BOOTSTRAP_SERVERS = System.getenv("BOOTSTRAP_SERVERS");
    public static final String CLUSTER_API_KEY = System.getenv("CLUSTER_API_KEY");
    public static final String CLUSTER_API_SECRET = System.getenv("CLUSTER_API_SECRET");
    public static final String ORIGIN_ALLOWED = System.getenv("ORIGIN_ALLOWED");

    public static final String SCOREBOARD_FIELD = "scoreboard";
    public static final String SCOREBOARD_TOPIC = "SCOREBOARD";
    public static final String HIGHEST_SCORE_FIELD = "HIGHEST_SCORE";
    public static final String HIGHEST_LEVEL_FIELD = "HIGHEST_LEVEL";
    public static final String TOTAL_LOSSES_FIELD = "TOTAL_LOSSES";
    public static final String USER_FIELD = "USER";

    public static final String BEST_PLAYER_INTENT = "BestPlayerIntent";
    public static final String TOPN_PLAYERS_INTENT = "TopNPlayersIntent";
    public static final String PLAYER_DETAILS_INTENT = "PlayerDetailsIntent";
    public static final String NUMBER_OF_PLAYERS_SLOT = "numberOfPlayers";
    public static final String PLAYER_NAME_SLOT = "playerName";
    public static final String POSITION_RELATIVE_SLOT = "positionRelative";
    public static final String POSITION_ABSOLUTE_SLOT = "positionAbsolute";

}
