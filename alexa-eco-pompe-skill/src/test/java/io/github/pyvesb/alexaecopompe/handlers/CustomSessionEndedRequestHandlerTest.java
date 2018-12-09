package io.github.pyvesb.alexaecopompe.handlers;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.InputBuilder.buildEndedInput;

import org.junit.jupiter.api.Test;

import com.amazon.ask.model.Response;

import utils.MissingResponse;

class CustomSessionEndedRequestHandlerTest {

	private final CustomSessionEndedRequestHandler underTest = new CustomSessionEndedRequestHandler();

	@Test
	void shouldHandleSessionEndedRequests() {
		assertTrue(underTest.canHandle(buildEndedInput()));
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
