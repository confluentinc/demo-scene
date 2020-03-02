package io.confluent.cloud.pacman;

import java.util.List;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;

import io.confluent.cloud.pacman.alexa.AlexaDetailsHandler;
import io.confluent.cloud.pacman.alexa.AlexaHelpHandler;
import io.confluent.cloud.pacman.alexa.AlexaPlayersHandler;
import io.confluent.cloud.pacman.alexa.AlexaStopHandler;

import com.amazon.ask.SkillStreamHandler;

public class AlexaHandler extends SkillStreamHandler {

	private static Skill getSkill() {
		return Skills.standard()
			.addRequestHandlers(List.of(
				new AlexaHelpHandler(),
				new AlexaPlayersHandler(),
				new AlexaDetailsHandler(),
				new AlexaStopHandler()
			))
			.build();

	}

    public AlexaHandler() {
        super(getSkill());
    }

}
