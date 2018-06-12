package io.github.pyvesb.alexaecopompe.handlers;

import static com.amazon.ask.request.Predicates.intentName;
import static io.github.pyvesb.alexaecopompe.speech.Messages.CANCEL_STOP;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response;

public class CancelStopIntentHandler implements RequestHandler {

	private static final Logger LOGGER = LogManager.getLogger(CancelStopIntentHandler.class);

	@Override
	public boolean canHandle(HandlerInput input) {
		return input.matches(intentName("AMAZON.CancelIntent")) || input.matches(intentName("AMAZON.StopIntent"));
	}

	@Override
	public Optional<Response> handle(HandlerInput input) {
		RequestEnvelope envelope = input.getRequestEnvelope();
		LOGGER.info("Cancel/stop intent (session={}, intent={})", envelope.getSession().getSessionId(),
				((IntentRequest) envelope.getRequest()).getIntent().getName());
		return input.getResponseBuilder()
				.withSpeech(CANCEL_STOP)
				.withShouldEndSession(true)
				.build();
	}

}
