package io.github.pyvesb.alexaecopompe.handlers;

import static io.github.pyvesb.alexaecopompe.speech.Messages.LAUNCH;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.LaunchRequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;

public class CustomLaunchRequestHandler implements LaunchRequestHandler {

	private static final Logger LOGGER = LogManager.getLogger(CustomLaunchRequestHandler.class);

	@Override
	public boolean canHandle(HandlerInput input, LaunchRequest launchRequest) {
		return true;
	}

	@Override
	public Optional<Response> handle(HandlerInput input, LaunchRequest launchRequest) {
		LOGGER.info("Launch request (session={})", input.getRequestEnvelope().getSession().getSessionId());
		return input.getResponseBuilder()
				.withSpeech(LAUNCH)
				.withReprompt(LAUNCH)
				.build();
	}

}
