package io.github.pyvesb.alexaecopompe.handlers;

import java.util.Optional;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.SessionEndedRequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.SessionEndedRequest;

public class CustomSessionEndedRequestHandler implements SessionEndedRequestHandler {

	@Override
	public boolean canHandle(HandlerInput input, SessionEndedRequest sessionEndedRequest) {
		return true;
	}

	@Override
	public Optional<Response> handle(HandlerInput input, SessionEndedRequest sessionEndedRequest) {
		return input.getResponseBuilder().build();
	}

}
