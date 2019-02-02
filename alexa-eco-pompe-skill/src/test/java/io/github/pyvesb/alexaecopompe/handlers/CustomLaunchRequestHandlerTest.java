package io.github.pyvesb.alexaecopompe.handlers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.InputBuilder.buildLaunchInput;
import static utils.ResponseAssertions.assertSpeech;

import org.junit.jupiter.api.Test;

import com.amazon.ask.model.Response;

import utils.MissingResponse;

class CustomLaunchRequestHandlerTest {

	private final CustomLaunchRequestHandler underTest = new CustomLaunchRequestHandler();

	@Test
	void shouldHandleLaunchRequests() {
		assertTrue(underTest.canHandle(buildLaunchInput()));
	}

	@Test
	void shouldReturnLaunchResponse() {
		Response resp = underTest.handle(buildLaunchInput()).orElseThrow(MissingResponse::new);

		assertFalse(resp.getShouldEndSession());
		assertSpeech("Demandez-moi la pompe la moins chère dans une ville, un département ou près de vous. Pour plus "
				+ "d'informations, dîtes \"aide\".", resp);
		assertNull(resp.getCard());
	}

}
