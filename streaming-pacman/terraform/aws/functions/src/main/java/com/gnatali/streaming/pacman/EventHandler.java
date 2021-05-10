package com.gnatali.streaming.pacman;

import java.util.Map;
import java.io.IOException;
import java.util.HashMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.gnatali.streaming.pacman.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.exception.ExceptionUtils;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

import static com.gnatali.streaming.pacman.utils.Constants.*;

public class EventHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    public static final MediaType MEDIATYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIATYPE_KSQL = MediaType.parse("application/vnd.ksql.v1+json; charset=utf-8");
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    OkHttpClient client = new OkHttpClient.Builder().authenticator(new Authenticator() {

        @Override
        public Request authenticate(Route route, Response response) throws IOException {
            if (response.request().header("Authorization") != null) {
                return null; // Give up, we've already attempted to authenticate.
            }

            System.out.println("Authenticating for response: " + response);
            System.out.println("Challenges: " + response.challenges());
            String basicAuth = Constants.KSQLDB_API_AUTH_INFO;
            String credential = Credentials.basic(basicAuth.split(":")[0], basicAuth.split(":")[1]);
            return response.request().newBuilder().header("Authorization", credential).build();
        }
    })
    .build();
  

    private String post(String url, String json, String accept) throws IOException {
        RequestBody body = RequestBody.create(MEDIATYPE_KSQL, json);
        okhttp3.Request.Builder requestBuilder = new Request.Builder()
            .url(url);

            if(accept != null){
                requestBuilder.addHeader("Accept", accept);
            }
            

            Request request = requestBuilder
            .post(body)
            .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public Map<String, Object> handleRequest(final Map<String, Object> request, final Context context) {

        LambdaLogger logger = context.getLogger();

        logger.log("ENVIRONMENT VARIABLES: " + gson.toJson(System.getenv()));
        logger.log("CONTEXT: " + gson.toJson(context));

        String result;
        
        Map<String, Object> response = new HashMap<>();
        if (!request.containsKey(HEADERS_KEY)) {
            result = "Thanks for waking me up" ;
            response.put(BODY_KEY, result);
            logger.log("Function wake up received");
            return response;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> requestHeaders =
            (Map<String, Object>) request.get(HEADERS_KEY);

        if (requestHeaders.containsKey(ORIGIN_KEY)) {

            String origin = (String) requestHeaders.get(ORIGIN_KEY);
            logger.log("Function origin is "+origin);
            logger.log("Origin Allowed is "+ORIGIN_ALLOWED);

            if (origin.equals(ORIGIN_ALLOWED)) {

                if (request.containsKey(BODY_KEY)) {

                    String event = (String) request.get(BODY_KEY);

                    logger.log("EVENT: " + gson.toJson(event));
                    logger.log("EVENT TYPE: " + event.getClass().toString());

                    if (event != null ) {

                        JsonElement payloadRoot = JsonParser.parseString(event);
                        
                        String payloadEndpoint = payloadRoot.getAsJsonObject().get("endpoint").getAsString();
                        String payloadQuery;
                       
                        logger.log("payloadEndpoint: "+payloadEndpoint);

                        String endpoint;
                        String queryObjName;
                        String accept = null;
                        
                        
                        if(Constants.KSQLDB_ENDPOINT_QUERY.equals(payloadEndpoint)){
                            endpoint = Constants.KSQLDB_ENDPOINT_QUERY;
                            queryObjName = "sql";
                            accept = "application/json";
                        }else if(Constants.KSQLDB_ENDPOINT_KSQL.equals(payloadEndpoint)){
                            endpoint = Constants.KSQLDB_ENDPOINT_KSQL;
                            queryObjName = "ksql";
                            
                        } else {
                            StringBuilder message = new StringBuilder();
                            message.append("The endpoint provided ("+payloadEndpoint+") is not supported");
                            result = message.toString();
                            response.put(BODY_KEY, result);

                            return response;
                        }

                        payloadQuery = payloadRoot.getAsJsonObject().get(queryObjName).getAsString();
                        logger.log("payloadQuery: "+payloadQuery);

                        JsonObject newPayload = new JsonObject();
                        newPayload.add(queryObjName, payloadRoot.getAsJsonObject().get(queryObjName));
                        

                        try {
                            logger.log("Sending POST to : "+Constants.KSQLDB_ENDPOINT + "/" + endpoint);
                            logger.log("Payload : "+ gson.toJson(newPayload));
                            result = post(Constants.KSQLDB_ENDPOINT + "/" + endpoint, gson.toJson(newPayload), accept);
                            logger.log("Post worked: "+endpoint+" result: "+result);
                        } catch (Exception e) {
                            logger.log("Error! "+e.getMessage());
                            logger.log(ExceptionUtils.getStackTrace(e));
                            StringBuilder message = new StringBuilder();
                            message.append("Error in executing the query ");
                            message.append(payloadQuery);
                            message.append(e.getMessage());
                            response.put(BODY_KEY, message.toString());

                            return response;
                        }

                        response.put(BODY_KEY, result);
                        
                        Map<String, Object> responseHeaders = new HashMap<>();
                        responseHeaders.put("Access-Control-Allow-Headers", "*");
                        responseHeaders.put("Access-Control-Allow-Methods", POST_METHOD);
                        responseHeaders.put("Access-Control-Allow-Origin", ORIGIN_ALLOWED);
                        response.put(HEADERS_KEY, responseHeaders);

                    }else{
                        logger.log("Didn't enter first IF!");
                    }
        
                }
                
            }
            
        }else {
            logger.log("No origin!");
        }
        
        logger.log("Response will be sent : "+ gson.toJson(response));
        return response;
    
    }


    

    

}
