package com.riferrei.streaming.pacman;

import java.util.List;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;

import com.riferrei.streaming.pacman.alexa.AlexaDetailsHandler;
import com.riferrei.streaming.pacman.alexa.AlexaHelpHandler;
import com.riferrei.streaming.pacman.alexa.AlexaPlayersHandler;
import com.riferrei.streaming.pacman.alexa.AlexaStopHandler;

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
