package io.confluent.cloud.pacman.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.handler.GenericRequestHandler;

import static com.amazon.ask.request.Predicates.intentName;

import java.util.Optional;

public class AlexaStopHandler implements GenericRequestHandler<HandlerInput, Optional<Response>> {

    @Override
    public boolean canHandle(HandlerInput input) {
        return
            input.matches(intentName("AMAZON.StopIntent")
            .or(intentName("AMAZON.CancelIntent")));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        return input.getResponseBuilder()
            .withSpeech("Goodbye")
            .build();
    }

}
