package io.confluent.cloud.pacman.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.handler.GenericRequestHandler;

import static com.amazon.ask.request.Predicates.intentName;

import java.util.Optional;

public class AlexaHelpHandler implements GenericRequestHandler<HandlerInput, Optional<Response>> {

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.HelpIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        String speechText = "You can ask who is winning the game";
        return input.getResponseBuilder()
            .withSpeech(speechText)
            .withReprompt(speechText)
            .build();
    }

}
