package com.riferrei.streaming.pacman.alexa;

import java.util.Optional;
import java.util.ResourceBundle;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.handler.GenericRequestHandler;
import static com.amazon.ask.request.Predicates.intentName;

import static com.riferrei.streaming.pacman.utils.Constants.*;
import static com.riferrei.streaming.pacman.utils.SkillUtils.*;

public class AlexaStopHandler implements GenericRequestHandler<HandlerInput, Optional<Response>> {

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.StopIntent")
            .or(intentName("AMAZON.CancelIntent")));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {

        ResourceBundle resourceBundle = getResourceBundle(input);
        String speechText = resourceBundle.getString(GOODBYE);
        return input.getResponseBuilder()
            .withSpeech(speechText)
            .withReprompt(speechText)
            .build();
            
    }

}
