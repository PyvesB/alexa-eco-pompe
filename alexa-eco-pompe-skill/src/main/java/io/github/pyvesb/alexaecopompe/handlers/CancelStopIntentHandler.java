package io.github.pyvesb.alexaecopompe.handlers;

import static io.github.pyvesb.alexaecopompe.speech.Messages.CANCEL_STOP;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;

public class CancelStopIntentHandler implements IntentRequestHandler {

	private static final Logger LOGGER = LogManager.getLogger(CancelStopIntentHandler.class);

	@Override
	public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
		String intentName = intentRequest.getIntent().getName();
		return "AMAZON.CancelIntent".equals(intentName) || "AMAZON.StopIntent".equals(intentName);
	}

	@Override
	public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
		LOGGER.info("Cancel/stop intent (session={}, intent={})", input.getRequestEnvelope().getSession().getSessionId(),
				intentRequest.getIntent().getName());
		return input.getResponseBuilder()
				.withSpeech(CANCEL_STOP)
				.withShouldEndSession(true)
				.build();
	}

}
