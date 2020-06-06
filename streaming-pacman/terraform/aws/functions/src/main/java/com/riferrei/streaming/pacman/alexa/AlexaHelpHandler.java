package com.riferrei.streaming.pacman.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.handler.GenericRequestHandler;

import static com.amazon.ask.request.Predicates.intentName;

import java.util.Optional;
import java.util.ResourceBundle;

import static com.riferrei.streaming.pacman.utils.Constants.*;
import static com.riferrei.streaming.pacman.utils.SkillUtils.*;

public class AlexaHelpHandler implements GenericRequestHandler<HandlerInput, Optional<Response>> {

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.HelpIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {

        ResourceBundle resourceBundle = getResourceBundle(input);
        String speechText = resourceBundle.getString(ALEXA_HELP);
        return input.getResponseBuilder()
            .withSpeech(speechText)
            .withReprompt(speechText)
            .build();
            
    }

}
