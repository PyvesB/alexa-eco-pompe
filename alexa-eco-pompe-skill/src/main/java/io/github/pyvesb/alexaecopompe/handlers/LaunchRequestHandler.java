package io.github.pyvesb.alexaecopompe.handlers;

import static com.amazon.ask.request.Predicates.requestType;
import static io.github.pyvesb.alexaecopompe.speech.Messages.LAUNCH;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;

public class LaunchRequestHandler implements RequestHandler {

	private static final Logger LOGGER = LogManager.getLogger(LaunchRequestHandler.class);

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(requestType(LaunchRequest.class));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		LOGGER.info("Launch request (session={})", input.getRequestEnvelope().getSession().getSessionId());
		return input.getResponseBuilder()
				.withSpeech(LAUNCH)
				.withReprompt(LAUNCH)
				.build();
	}

}
