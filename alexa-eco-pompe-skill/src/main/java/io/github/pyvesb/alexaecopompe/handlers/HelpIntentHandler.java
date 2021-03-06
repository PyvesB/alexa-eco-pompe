package io.github.pyvesb.alexaecopompe.handlers;

import static io.github.pyvesb.alexaecopompe.speech.Messages.CARD_HELP;
import static io.github.pyvesb.alexaecopompe.speech.Messages.HELP;
import static io.github.pyvesb.alexaecopompe.speech.Messages.NAME;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;

public class HelpIntentHandler implements IntentRequestHandler {

	private static final Logger LOGGER = LogManager.getLogger(HelpIntentHandler.class);

	@Override
	public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
		return "AMAZON.HelpIntent".equals(intentRequest.getIntent().getName());
	}

	@Override
	public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
		LOGGER.info("Help intent (session={})", input.getRequestEnvelope().getSession().getSessionId());
		return input.getResponseBuilder()
				.withSpeech(HELP)
				.withSimpleCard(NAME, CARD_HELP)
				.withReprompt(HELP)
				.build();
	}

}
