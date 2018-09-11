package io.github.pyvesb.alexaecopompe.handlers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.InputBuilder.buildIntentInput;
import static utils.InputBuilder.buildLaunchInput;
import static utils.ResponseAssertions.assertSpeech;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.amazon.ask.model.Response;

import utils.MissingResponse;

class CancelStopIntentHandlerTest {

	private final CancelStopIntentHandler underTest = new CancelStopIntentHandler();

	@ParameterizedTest
	@ValueSource(strings = { "AMAZON.CancelIntent", "AMAZON.StopIntent" })
	void shouldHandleIntentRequestsWithCancelOrStopIntentName(String intentName) {
		assertTrue(underTest.canHandle(buildIntentInput(intentName)));
	}

	@Test
	void shouldNotHandleIntentRequestsWithDifferentName() {
		assertFalse(underTest.canHandle(buildIntentInput("AMAZON.HelpIntent")));
	}

	@Test
	void shouldNotHandleOtherRequests() {
		assertFalse(underTest.canHandle(buildLaunchInput()));
	}

	@Test
	void shouldReturnCancelStopResponse() {
		Response resp = underTest.handle(buildIntentInput("AMAZON.CancelIntent")).orElseThrow(MissingResponse::new);

		assertTrue(resp.getShouldEndSession());
		assertSpeech("À bientôt sur Éco Pompe !", resp);
		assertNull(resp.getCard());
	}

}
