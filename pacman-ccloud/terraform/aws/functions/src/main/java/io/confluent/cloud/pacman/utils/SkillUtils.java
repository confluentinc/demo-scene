package io.confluent.cloud.pacman.utils;

import java.util.Locale;
import java.util.ResourceBundle;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;

public class SkillUtils {

    public static ResourceBundle getResourceBundle(HandlerInput input) {
        Locale locale = new Locale(input.getRequestEnvelope().getRequest().getLocale());
        return ResourceBundle.getBundle(Constants.SPEECH_TEXT, locale);
    }

}
