package io.github.pyvesb.alexaecopompe.handlers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.InputBuilder.buildEndedInput;

import org.junit.jupiter.api.Test;

import com.amazon.ask.model.Response;

import utils.InputBuilder;
import utils.MissingResponse;

class SessionEndedRequestHandlerTest {

	private final SessionEndedRequestHandler underTest = new SessionEndedRequestHandler();

	@Test
	void shouldHandleSessionEndedRequests() {
		assertTrue(underTest.canHandle(buildEndedInput()));
	}

	@Test
	void shouldNotHandleOtherRequests() {
		assertFalse(underTest.canHandle(InputBuilder.buildIntentInput("GasTown")));
	}

	@Test
	void shouldReturnEmptyResponse() {
		Response response = underTest.handle(buildEndedInput()).orElseThrow(MissingResponse::new);

		assertNull(response.getShouldEndSession());
		assertNull(response.getOutputSpeech());
		assertNull(response.getReprompt());
		assertNull(response.getCard());
	}

}
